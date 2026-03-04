import { request } from '@umijs/max';

type UmiRequestOptions = NonNullable<Parameters<typeof request>[1]>;

export interface HttpRequestOptions extends UmiRequestOptions {
    skipDefaultHeaders?: boolean;
    skipAuth?: boolean;
    requireBody?: boolean;
    customHeaders?: Record<string, string>;
}

const BODY_METHODS = new Set(['POST', 'PUT', 'PATCH']);

const buildRequestId = () =>
    `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

const getStorageValue = (key: string): string | null => {
    if (typeof window === 'undefined') {
        return null;
    }
    return window.localStorage.getItem(key) || window.sessionStorage.getItem(key);
};

const getAuthorization = (): string | null => {
    const token = getStorageValue('token') || getStorageValue('accessToken');
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
    headers: UmiRequestOptions['headers'],
): Record<string, string> => {
    if (!headers) {
        return {};
    }
    if (headers instanceof Headers) {
        const result: Record<string, string> = {};
        headers.forEach((value, key) => {
            result[key] = value;
        });
        return result;
    }
    if (Array.isArray(headers)) {
        return headers.reduce<Record<string, string>>((acc, [key, value]) => {
            acc[String(key)] = String(value);
            return acc;
        }, {});
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

export async function httpRequest<T = any>(
    url: string,
    options: HttpRequestOptions = {},
) {
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

    return request<T>(url, {
        ...requestOptions,
        headers,
    });
}
