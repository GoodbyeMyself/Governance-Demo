export type AuthPersistence = 'local' | 'session';

export interface AuthStorageOptions {
    tokenKey?: string;
    userKey?: string;
    persistenceKey?: string;
    persistence?: AuthPersistence;
}

export interface RememberedLoginCredentials {
    username: string;
    password: string;
}
