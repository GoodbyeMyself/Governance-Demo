import { getLocale, translate } from '@governance/i18n';
import type { AxiosRequestConfig } from 'axios';
import axios, { isAxiosError } from 'axios';
import { clearAuthState, getCookieValue, getToken } from '../auth';

export interface HttpRequestOptions extends AxiosRequestConfig {
    skipDefaultHeaders?: boolean;
    skipAuth?: boolean;
    requireBody?: boolean;
    silentUnauthorized?: boolean;
    customHeaders?: Record<string, string>;
}

export interface HttpClientConfig {
    clientAppName?: string;
    loginPath?: string;
    getToken?: () => string | null;
    getStorageValue?: (key: string) => string | null;
    clearAuthState?: () => void;
    onUnauthorized?: (messageText: string) => void;
}

const BODY_METHODS = new Set(['POST', 'PUT', 'PATCH']);
const DEFAULT_LOGIN_PATH = '/login';
const DEFAULT_CLIENT_APP_NAME = 'governance-frontend';

const defaultGetStorageValue = (key: string): string | null => {
    return getCookieValue(key);
};

let httpClientConfig: HttpClientConfig = {
    clientAppName: DEFAULT_CLIENT_APP_NAME,
    loginPath: DEFAULT_LOGIN_PATH,
};

const buildRequestId = () =>
    `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

const normalizeToken = (token?: string | null) => {
    if (!token) {
        return null;
    }

    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
};

const getRuntimeConfig = (): Required<
    Pick<
        HttpClientConfig,
        'clientAppName' | 'loginPath' | 'getStorageValue' | 'clearAuthState'
    >
> &
    Pick<HttpClientConfig, 'getToken' | 'onUnauthorized'> => ({
    clientAppName: httpClientConfig.clientAppName || DEFAULT_CLIENT_APP_NAME,
    loginPath: httpClientConfig.loginPath || DEFAULT_LOGIN_PATH,
    getToken: httpClientConfig.getToken,
    getStorageValue: httpClientConfig.getStorageValue || defaultGetStorageValue,
    clearAuthState: httpClientConfig.clearAuthState || clearAuthState,
    onUnauthorized: httpClientConfig.onUnauthorized,
});

const getAuthorization = (): string | null => {
    const config = getRuntimeConfig();
    const token =
        config.getToken?.() ||
        getToken() ||
        config.getStorageValue('accessToken');

    return normalizeToken(token);
};

const getDefaultHeaders = (): Record<string, string> => {
    const config = getRuntimeConfig();
    const headers: Record<string, string> = {
        'X-Request-Id': buildRequestId(),
        'X-Request-Time': new Date().toISOString(),
        'X-Client-App': config.clientAppName,
        'Accept-Language': getLocale(),
    };

    const tenantId = config.getStorageValue('tenantId');
    if (tenantId) {
        headers['X-Tenant-Id'] = tenantId;
    }

    return headers;
};

const normalizeHeaders = (
    headers: AxiosRequestConfig['headers'],
): Record<string, string> => {
    if (!headers || typeof headers !== 'object') {
        return {};
    }

    return Object.entries(headers as Record<string, unknown>).reduce<
        Record<string, string>
    >((acc, [key, value]) => {
        if (value !== undefined && value !== null) {
            acc[key] = String(value);
        }

        return acc;
    }, {});
};

const validateBeforeRequest = (url: string, options: HttpRequestOptions) => {
    if (!url?.trim()) {
        throw new Error(translate('http.requestUrlRequired'));
    }

    const method = String(options.method || 'GET').toUpperCase();
    if (
        options.requireBody &&
        BODY_METHODS.has(method) &&
        (options.data === undefined || options.data === null)
    ) {
        throw new Error(
            translate('http.requestDataRequired', {
                method,
            }),
        );
    }
};

const resolveBusinessMessage = (payload: unknown): string | null => {
    if (!payload || typeof payload !== 'object') {
        return null;
    }

    const messageText = (payload as Record<string, unknown>).message;
    if (typeof messageText === 'string' && messageText.trim()) {
        return messageText.trim();
    }

    return null;
};

const resolveHttpMessage = (
    status: number,
    payload?: unknown,
    fallback?: string,
): string => {
    const businessMessage = resolveBusinessMessage(payload);
    if (businessMessage) {
        return businessMessage;
    }

    if (fallback) {
        return fallback;
    }

    switch (status) {
        case 400:
            return translate('http.400');
        case 401:
            return translate('http.401');
        case 403:
            return translate('http.403');
        case 404:
            return translate('http.404');
        case 405:
            return translate('http.405');
        case 408:
            return translate('http.408');
        case 409:
            return translate('http.409');
        case 422:
            return translate('http.422');
        case 500:
        case 502:
        case 503:
        case 504:
            return translate('http.500');
        default:
            return `${translate('http.default')} (HTTP ${status})`;
    }
};

const redirectToLogin = () => {
    if (typeof window === 'undefined') {
        return;
    }

    const { loginPath } = getRuntimeConfig();
    if (!loginPath || window.location.pathname === loginPath) {
        return;
    }

    const search = window.location.search || '';
    const redirect = encodeURIComponent(`${window.location.pathname}${search}`);
    window.location.replace(`${loginPath}?redirect=${redirect}`);
};

const handleHttpStatus = (
    status: number,
    payload?: unknown,
    fallback?: string,
    options?: Pick<HttpRequestOptions, 'silentUnauthorized'>,
): never => {
    const messageText = resolveHttpMessage(status, payload, fallback);

    if (status === 401) {
        const config = getRuntimeConfig();
        config.clearAuthState();
        if (!options?.silentUnauthorized) {
            config.onUnauthorized?.(messageText);
            redirectToLogin();
        }
        throw new Error(messageText);
    }

    throw new Error(messageText);
};

export const configureHttpClient = (config: HttpClientConfig) => {
    httpClientConfig = {
        ...httpClientConfig,
        ...config,
    };
};

export const resetHttpClientConfig = () => {
    httpClientConfig = {
        clientAppName: DEFAULT_CLIENT_APP_NAME,
        loginPath: DEFAULT_LOGIN_PATH,
    };
};

export async function httpRequest<T = unknown>(
    url: string,
    options: HttpRequestOptions = {},
): Promise<T> {
    validateBeforeRequest(url, options);

    const {
        skipDefaultHeaders,
        skipAuth,
        requireBody: _requireBody,
        silentUnauthorized,
        customHeaders,
        ...requestOptions
    } = options;

    const headers: Record<string, string> = {
        ...(skipDefaultHeaders ? {} : getDefaultHeaders()),
        ...normalizeHeaders(requestOptions.headers),
        ...(customHeaders || {}),
    };

    if (!skipAuth) {
        const authorization = getAuthorization();
        if (authorization && !headers.Authorization) {
            headers.Authorization = authorization;
        }
    }

    try {
        const response = await axios.request<T>({
            ...requestOptions,
            url,
            headers,
            withCredentials: true,
            validateStatus: () => true,
        });

        if (response.status < 200 || response.status >= 300) {
            handleHttpStatus(response.status, response.data, undefined, {
                silentUnauthorized,
            });
        }

        return response.data;
    } catch (error) {
        if (isAxiosError(error)) {
            const responseStatus = error.response?.status;
            const responseData = error.response?.data;
            if (responseStatus) {
                handleHttpStatus(responseStatus, responseData, undefined, {
                    silentUnauthorized,
                });
            }
        }

        const fallbackMessage =
            error instanceof Error && error.message
                ? error.message
                : translate('http.networkError');

        throw new Error(fallbackMessage);
    }
}
