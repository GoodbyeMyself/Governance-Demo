import axios, { isAxiosError } from 'axios';
import type { AxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { clearAuthState, getToken } from './auth';

export interface HttpRequestOptions extends AxiosRequestConfig {
    skipDefaultHeaders?: boolean;
    skipAuth?: boolean;
    requireBody?: boolean;
    customHeaders?: Record<string, string>;
}

const BODY_METHODS = new Set(['POST', 'PUT', 'PATCH']);
const LOGIN_PATH = '/login';

const buildRequestId = () =>
    `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

const getStorageValue = (key: string): string | null => {
    if (typeof window === 'undefined') {
        return null;
    }
    return window.localStorage.getItem(key) || window.sessionStorage.getItem(key);
};

const getAuthorization = (): string | null => {
    const token = getToken() || getStorageValue('accessToken');
    if (!token) {
        return null;
    }
    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
};

const getDefaultHeaders = (): Record<string, string> => {
    const headers: Record<string, string> = {
        'X-Request-Id': buildRequestId(),
        'X-Request-Time': new Date().toISOString(),
        'X-Client-App': 'governance-frontend',
    };

    const tenantId = getStorageValue('tenantId');
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
        throw new Error('请求地址不能为空');
    }

    const method = String(options.method || 'GET').toUpperCase();
    if (
        options.requireBody &&
        BODY_METHODS.has(method) &&
        (options.data === undefined || options.data === null)
    ) {
        throw new Error(`${method} 请求缺少 data 参数`);
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
            return '请求参数错误，请检查输入内容';
        case 401:
            return '登录已过期，请重新登录';
        case 403:
            return '暂无权限访问当前资源';
        case 404:
            return '请求的接口不存在';
        case 405:
            return '请求方法不支持';
        case 408:
            return '请求超时，请稍后重试';
        case 409:
            return '资源冲突，请检查后重试';
        case 413:
            return '请求内容过大';
        case 415:
            return '请求格式不受支持';
        case 429:
            return '请求过于频繁，请稍后再试';
        case 500:
            return '服务端异常，请稍后重试';
        case 502:
            return '网关错误，请稍后重试';
        case 503:
            return '服务暂不可用，请稍后重试';
        case 504:
            return '网关超时，请稍后重试';
        default:
            return `请求失败（HTTP ${status}）`;
    }
};

const redirectToLogin = () => {
    if (typeof window === 'undefined') {
        return;
    }

    const pathname = window.location.pathname;
    if (pathname === LOGIN_PATH) {
        return;
    }

    const search = window.location.search || '';
    const redirect = encodeURIComponent(`${pathname}${search}`);
    window.location.replace(`${LOGIN_PATH}?redirect=${redirect}`);
};

const handleHttpStatus = (
    status: number,
    payload?: unknown,
    fallback?: string,
): never => {
    const messageText = resolveHttpMessage(status, payload, fallback);

    if (status === 401) {
        clearAuthState();
        message.warning(messageText);
        redirectToLogin();
        throw new Error(messageText);
    }

    throw new Error(messageText);
};

export async function httpRequest<T = any>(
    url: string,
    options: HttpRequestOptions = {},
): Promise<T> {
    validateBeforeRequest(url, options);

    const {
        skipDefaultHeaders,
        skipAuth,
        requireBody: _requireBody,
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
        if (authorization) {
            headers.Authorization = authorization;
        }
    } else {
        delete headers.Authorization;
    }

    try {
        const response = await axios.request<T>({
            ...requestOptions,
            url,
            headers,
            validateStatus: () => true,
        });

        if (response.status < 200 || response.status >= 300) {
            handleHttpStatus(response.status, response.data);
        }

        return response.data;
    } catch (error) {
        if (isAxiosError(error)) {
            const responseStatus = error.response?.status;
            const responseData = error.response?.data;
            if (responseStatus) {
                handleHttpStatus(responseStatus, responseData);
            }
        }

        const fallbackMessage =
            error instanceof Error && error.message
                ? error.message
                : '网络异常，请检查网络连接';
        throw new Error(fallbackMessage);
    }
}
