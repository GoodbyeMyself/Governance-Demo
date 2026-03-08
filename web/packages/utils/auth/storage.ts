import type { AuthStorageOptions } from './types';

const DEFAULT_TOKEN_KEY = 'token';
const DEFAULT_USER_KEY = 'authUser';

const getStorage = () => {
    if (typeof window === 'undefined') {
        return null;
    }

    return window.localStorage;
};

const resolveKeys = (options?: AuthStorageOptions) => ({
    tokenKey: options?.tokenKey || DEFAULT_TOKEN_KEY,
    userKey: options?.userKey || DEFAULT_USER_KEY,
});

export const getToken = (options?: AuthStorageOptions): string | null => {
    const storage = getStorage();
    if (!storage) {
        return null;
    }

    const { tokenKey } = resolveKeys(options);
    return storage.getItem(tokenKey);
};

export const hasToken = (options?: AuthStorageOptions): boolean =>
    Boolean(getToken(options));

export const setAuthState = <TUser>(
    token: string,
    user: TUser | null | undefined,
    options?: AuthStorageOptions,
) => {
    const storage = getStorage();
    if (!storage) {
        return;
    }

    const { tokenKey, userKey } = resolveKeys(options);
    storage.setItem(tokenKey, token);
    if (user) {
        storage.setItem(userKey, JSON.stringify(user));
    } else {
        storage.removeItem(userKey);
    }
};

export const clearAuthState = (options?: AuthStorageOptions) => {
    const storage = getStorage();
    if (!storage) {
        return;
    }

    const { tokenKey, userKey } = resolveKeys(options);
    storage.removeItem(tokenKey);
    storage.removeItem(userKey);
};

export const getStoredUser = <TUser = unknown>(
    options?: AuthStorageOptions,
): TUser | null => {
    const storage = getStorage();
    if (!storage) {
        return null;
    }

    const { userKey } = resolveKeys(options);
    const value = storage.getItem(userKey);
    if (!value) {
        return null;
    }

    try {
        return JSON.parse(value) as TUser;
    } catch {
        return null;
    }
};
