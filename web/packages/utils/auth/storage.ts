import type {
    AuthPersistence,
    AuthStorageOptions,
    RememberedLoginCredentials,
} from './types';

const DEFAULT_TOKEN_KEY = 'token';
const DEFAULT_USER_KEY = 'authUser';
const DEFAULT_PERSISTENCE_KEY = 'authPersistence';
const DEFAULT_REMEMBERED_LOGIN_KEY = 'rememberedLoginCredentials';
const DEFAULT_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

const hasDocument = () => typeof document !== 'undefined';

const getBrowserStorage = (persistence: AuthPersistence) => {
    if (typeof window === 'undefined') {
        return null;
    }

    return persistence === 'session'
        ? window.sessionStorage
        : window.localStorage;
};

const resolveKeys = (options?: AuthStorageOptions) => ({
    tokenKey: options?.tokenKey || DEFAULT_TOKEN_KEY,
    userKey: options?.userKey || DEFAULT_USER_KEY,
    persistenceKey: options?.persistenceKey || DEFAULT_PERSISTENCE_KEY,
});

const encodeCookieValue = (value: string) => encodeURIComponent(value);

const decodeCookieValue = (value: string) => {
    try {
        return decodeURIComponent(value);
    } catch {
        return value;
    }
};

export const getCookieValue = (key: string): string | null => {
    if (!hasDocument()) {
        return null;
    }

    const cookiePrefix = `${key}=`;
    const cookie = document.cookie
        .split(';')
        .map((item) => item.trim())
        .find((item) => item.startsWith(cookiePrefix));

    if (!cookie) {
        return null;
    }

    return decodeCookieValue(cookie.slice(cookiePrefix.length));
};

const setCookieValue = (
    key: string,
    value: string,
    persistence: AuthPersistence,
    maxAgeSeconds = DEFAULT_COOKIE_MAX_AGE_SECONDS,
) => {
    if (!hasDocument()) {
        return;
    }

    const segments = [
        `${key}=${encodeCookieValue(value)}`,
        'Path=/',
        'SameSite=Lax',
    ];

    if (persistence === 'local') {
        segments.push(`Max-Age=${maxAgeSeconds}`);
    }

    document.cookie = segments.join('; ');
};

const clearCookieValue = (key: string) => {
    if (!hasDocument()) {
        return;
    }

    document.cookie = `${key}=; Path=/; SameSite=Lax; Max-Age=0`;
};

const removeFromLegacyStorages = (key: string) => {
    getBrowserStorage('local')?.removeItem(key);
    getBrowserStorage('session')?.removeItem(key);
};

export const getToken = (options?: AuthStorageOptions): string | null => {
    const { tokenKey } = resolveKeys(options);
    return getCookieValue(tokenKey);
};

export const hasToken = (options?: AuthStorageOptions): boolean =>
    Boolean(getToken(options));

export const getAuthPersistence = (
    options?: AuthStorageOptions,
): AuthPersistence => {
    const { persistenceKey } = resolveKeys(options);
    const persistence = getCookieValue(persistenceKey);
    return persistence === 'local' || persistence === 'session'
        ? persistence
        : options?.persistence || 'local';
};

export const setAuthState = <TUser>(
    _token: string | null | undefined,
    user: TUser | null | undefined,
    options?: AuthStorageOptions,
) => {
    const persistence = options?.persistence || 'local';
    const { tokenKey, userKey, persistenceKey } = resolveKeys(options);

    removeFromLegacyStorages(tokenKey);
    clearCookieValue(tokenKey);

    if (user) {
        setCookieValue(userKey, JSON.stringify(user), persistence);
        setCookieValue(persistenceKey, persistence, persistence);
        return;
    }

    clearCookieValue(userKey);
    clearCookieValue(persistenceKey);
};

export const clearAuthState = (options?: AuthStorageOptions) => {
    const { tokenKey, userKey, persistenceKey } = resolveKeys(options);

    clearCookieValue(userKey);
    clearCookieValue(persistenceKey);
    clearCookieValue(tokenKey);

    removeFromLegacyStorages(tokenKey);
    removeFromLegacyStorages(userKey);
    removeFromLegacyStorages(persistenceKey);
};

export const getStoredUser = <TUser = unknown>(
    options?: AuthStorageOptions,
): TUser | null => {
    const { userKey } = resolveKeys(options);
    const value = getCookieValue(userKey);
    if (!value) {
        return null;
    }

    try {
        return JSON.parse(value) as TUser;
    } catch {
        return null;
    }
};

export const getRememberedLoginCredentials =
    (): RememberedLoginCredentials | null => {
        const value = getCookieValue(DEFAULT_REMEMBERED_LOGIN_KEY);
        if (!value) {
            return null;
        }

        try {
            return JSON.parse(value) as RememberedLoginCredentials;
        } catch {
            return null;
        }
    };

export const setRememberedLoginCredentials = (
    credentials: RememberedLoginCredentials,
) => {
    setCookieValue(
        DEFAULT_REMEMBERED_LOGIN_KEY,
        JSON.stringify(credentials),
        'local',
    );
};

export const clearRememberedLoginCredentials = () => {
    clearCookieValue(DEFAULT_REMEMBERED_LOGIN_KEY);
};
