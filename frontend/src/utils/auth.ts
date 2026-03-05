import type { AuthCenterUserProfile } from '@/services/authCenter';

const TOKEN_KEY = 'token';
const USER_KEY = 'authUser';

const getStorage = () => {
    if (typeof window === 'undefined') {
        return null;
    }
    return window.localStorage;
};

export const getToken = (): string | null => {
    const storage = getStorage();
    if (!storage) {
        return null;
    }
    return storage.getItem(TOKEN_KEY);
};

export const hasToken = (): boolean => Boolean(getToken());

export const setAuthState = (
    token: string,
    user: AuthCenterUserProfile | null | undefined,
) => {
    const storage = getStorage();
    if (!storage) {
        return;
    }

    storage.setItem(TOKEN_KEY, token);
    if (user) {
        storage.setItem(USER_KEY, JSON.stringify(user));
    } else {
        storage.removeItem(USER_KEY);
    }
};

export const clearAuthState = () => {
    const storage = getStorage();
    if (!storage) {
        return;
    }
    storage.removeItem(TOKEN_KEY);
    storage.removeItem(USER_KEY);
};

export const getStoredUser = (): AuthCenterUserProfile | null => {
    const storage = getStorage();
    if (!storage) {
        return null;
    }
    const value = storage.getItem(USER_KEY);
    if (!value) {
        return null;
    }

    try {
        return JSON.parse(value) as AuthCenterUserProfile;
    } catch {
        return null;
    }
};

