import type {
    AuthPersistence,
    AuthStorageOptions,
    RememberedLoginCredentials,
} from './types';

const DEFAULT_TOKEN_KEY = 'token';
const DEFAULT_USER_KEY = 'authUser';
const DEFAULT_REMEMBERED_LOGIN_KEY = 'rememberedLoginCredentials';

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
});

const getStorageValue = (key: string): string | null => {
    const localStorage = getBrowserStorage('local');
    const sessionStorage = getBrowserStorage('session');

    return localStorage?.getItem(key) || sessionStorage?.getItem(key) || null;
};

const removeFromAllStorages = (key: string) => {
    getBrowserStorage('local')?.removeItem(key);
    getBrowserStorage('session')?.removeItem(key);
};

export const getToken = (options?: AuthStorageOptions): string | null => {
    const { tokenKey } = resolveKeys(options);
    return getStorageValue(tokenKey);
};

export const hasToken = (options?: AuthStorageOptions): boolean =>
    Boolean(getToken(options));

export const getAuthPersistence = (
    options?: AuthStorageOptions,
): AuthPersistence => {
    const { tokenKey } = resolveKeys(options);
    const localStorage = getBrowserStorage('local');
    if (localStorage?.getItem(tokenKey)) {
        return 'local';
    }

    const sessionStorage = getBrowserStorage('session');
    if (sessionStorage?.getItem(tokenKey)) {
        return 'session';
    }

    return options?.persistence || 'local';
};

export const setAuthState = <TUser>(
    token: string,
    user: TUser | null | undefined,
    options?: AuthStorageOptions,
) => {
    const persistence = options?.persistence || 'local';
    const storage = getBrowserStorage(persistence);
    if (!storage) {
        return;
    }

    const { tokenKey, userKey } = resolveKeys(options);
    removeFromAllStorages(tokenKey);
    removeFromAllStorages(userKey);

    storage.setItem(tokenKey, token);
    if (user) {
        storage.setItem(userKey, JSON.stringify(user));
    } else {
        storage.removeItem(userKey);
    }
};

export const clearAuthState = (options?: AuthStorageOptions) => {
    const { tokenKey, userKey } = resolveKeys(options);
    removeFromAllStorages(tokenKey);
    removeFromAllStorages(userKey);
};

export const getStoredUser = <TUser = unknown>(
    options?: AuthStorageOptions,
): TUser | null => {
    const { userKey } = resolveKeys(options);
    const value = getStorageValue(userKey);
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
        const localStorage = getBrowserStorage('local');
        const value = localStorage?.getItem(DEFAULT_REMEMBERED_LOGIN_KEY);
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
    const localStorage = getBrowserStorage('local');
    if (!localStorage) {
        return;
    }

    localStorage.setItem(
        DEFAULT_REMEMBERED_LOGIN_KEY,
        JSON.stringify(credentials),
    );
};

export const clearRememberedLoginCredentials = () => {
    getBrowserStorage('local')?.removeItem(DEFAULT_REMEMBERED_LOGIN_KEY);
};
