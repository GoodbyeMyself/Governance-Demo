import {
    AUTH_BRIDGE_PATH,
    buildGovernAppUrl,
    buildPortalAppUrl,
    getAppOrigin,
    getSiblingAppName,
    type AppName,
} from './routes';
import {
    clearAuthState,
    getAuthPersistence,
    getStoredUser,
    getToken,
} from './storage';
import type { AuthPersistence } from './types';

const DEFAULT_TIMEOUT_MS = 2500;

export const AUTH_BRIDGE_CHANNEL = 'governance-auth-bridge';

export type AuthBridgeRequestType = 'REQUEST_AUTH_STATE' | 'CLEAR_AUTH_STATE';
export type AuthBridgeResponseType =
    | 'AUTH_STATE'
    | 'AUTH_UNAVAILABLE'
    | 'AUTH_CLEARED';

export interface AuthBridgePayload<TUser = unknown> {
    token: string;
    user: TUser | null;
    persistence: AuthPersistence;
}

export interface AuthBridgeRequestMessage {
    channel: typeof AUTH_BRIDGE_CHANNEL;
    requestId: string;
    type: AuthBridgeRequestType;
}

export interface AuthBridgeResponseMessage<TUser = unknown> {
    channel: typeof AUTH_BRIDGE_CHANNEL;
    requestId: string;
    type: AuthBridgeResponseType;
    payload?: AuthBridgePayload<TUser>;
}

const hasWindow = () => typeof window !== 'undefined';

const buildAuthBridgeUrl = (appName: AppName) =>
    appName === 'govern'
        ? buildGovernAppUrl(AUTH_BRIDGE_PATH)
        : buildPortalAppUrl(AUTH_BRIDGE_PATH);

const createRequestId = () =>
    `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

const createHiddenIframe = (src: string) => {
    const iframe = document.createElement('iframe');
    iframe.src = src;
    iframe.setAttribute('aria-hidden', 'true');
    iframe.tabIndex = -1;
    iframe.style.position = 'absolute';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = '0';
    iframe.style.opacity = '0';
    iframe.style.pointerEvents = 'none';
    return iframe;
};

const cleanupIframe = (iframe: HTMLIFrameElement | null) => {
    if (iframe && iframe.parentNode) {
        iframe.parentNode.removeChild(iframe);
    }
};

const isBridgeResponseMessage = (
    value: unknown,
): value is AuthBridgeResponseMessage => {
    if (!value || typeof value !== 'object') {
        return false;
    }

    const data = value as Partial<AuthBridgeResponseMessage>;
    return (
        data.channel === AUTH_BRIDGE_CHANNEL &&
        typeof data.requestId === 'string' &&
        typeof data.type === 'string'
    );
};

export const isBridgeRequestMessage = (
    value: unknown,
): value is AuthBridgeRequestMessage => {
    if (!value || typeof value !== 'object') {
        return false;
    }

    const data = value as Partial<AuthBridgeRequestMessage>;
    return (
        data.channel === AUTH_BRIDGE_CHANNEL &&
        typeof data.requestId === 'string' &&
        (data.type === 'REQUEST_AUTH_STATE' ||
            data.type === 'CLEAR_AUTH_STATE')
    );
};

export const buildBridgeResponseMessage = <TUser = unknown>(
    requestId: string,
    type: AuthBridgeResponseType,
    payload?: AuthBridgePayload<TUser>,
): AuthBridgeResponseMessage<TUser> => ({
    channel: AUTH_BRIDGE_CHANNEL,
    requestId,
    type,
    payload,
});

export const readCurrentAuthBridgePayload = <TUser = unknown>():
    | AuthBridgePayload<TUser>
    | null => {
    const token = getToken();
    if (!token) {
        return null;
    }

    return {
        token,
        user: getStoredUser<TUser>(),
        persistence: getAuthPersistence(),
    };
};

export const clearCurrentAuthBridgePayload = () => {
    clearAuthState();
};

export const getAllowedAuthBridgeOrigins = () => {
    const origins = [getAppOrigin('govern'), getAppOrigin('portal')].filter(
        (origin): origin is string => Boolean(origin),
    );

    return Array.from(new Set(origins));
};

const postBridgeRequest = (
    appName: AppName,
    type: AuthBridgeRequestType,
    timeoutMs = DEFAULT_TIMEOUT_MS,
) =>
    new Promise<AuthBridgeResponseMessage | null>((resolve) => {
        if (!hasWindow() || !document.body) {
            resolve(null);
            return;
        }

        const bridgeUrl = buildAuthBridgeUrl(appName);
        const targetOrigin = getAppOrigin(appName);
        if (!bridgeUrl || !targetOrigin) {
            resolve(null);
            return;
        }

        const requestId = createRequestId();
        const iframe = createHiddenIframe(bridgeUrl);
        let settled = false;
        let timeoutId = 0;

        const finish = (result: AuthBridgeResponseMessage | null) => {
            if (settled) {
                return;
            }

            settled = true;
            window.clearTimeout(timeoutId);
            window.removeEventListener('message', handleMessage);
            cleanupIframe(iframe);
            resolve(result);
        };

        const handleMessage = (event: MessageEvent<unknown>) => {
            if (event.origin !== targetOrigin) {
                return;
            }

            if (!isBridgeResponseMessage(event.data)) {
                return;
            }

            if (event.data.requestId !== requestId) {
                return;
            }

            finish(event.data);
        };

        iframe.addEventListener(
            'load',
            () => {
                iframe.contentWindow?.postMessage(
                    {
                        channel: AUTH_BRIDGE_CHANNEL,
                        requestId,
                        type,
                    } satisfies AuthBridgeRequestMessage,
                    targetOrigin,
                );
            },
            { once: true },
        );

        iframe.addEventListener(
            'error',
            () => {
                finish(null);
            },
            { once: true },
        );

        window.addEventListener('message', handleMessage);
        timeoutId = window.setTimeout(() => finish(null), timeoutMs);
        document.body.appendChild(iframe);
    });

export const requestAuthStateFromApp = async <TUser = unknown>(
    appName: AppName,
    timeoutMs?: number,
): Promise<AuthBridgePayload<TUser> | null> => {
    const response = await postBridgeRequest(
        appName,
        'REQUEST_AUTH_STATE',
        timeoutMs,
    );

    if (response?.type !== 'AUTH_STATE' || !response.payload?.token) {
        return null;
    }

    return response.payload as AuthBridgePayload<TUser>;
};

export const requestAuthStateFromSibling = <TUser = unknown>(
    currentAppName: AppName,
    timeoutMs?: number,
) =>
    requestAuthStateFromApp<TUser>(
        getSiblingAppName(currentAppName),
        timeoutMs,
    );

export const clearAuthStateInApp = async (
    appName: AppName,
    timeoutMs?: number,
) => {
    const response = await postBridgeRequest(
        appName,
        'CLEAR_AUTH_STATE',
        timeoutMs,
    );

    return response?.type === 'AUTH_CLEARED';
};

export const clearAuthStateInSibling = (
    currentAppName: AppName,
    timeoutMs?: number,
) => clearAuthStateInApp(getSiblingAppName(currentAppName), timeoutMs);
