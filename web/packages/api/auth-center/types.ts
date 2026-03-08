export type AuthCenterUserRole = 'USER' | 'ADMIN';
export type AuthCenterUserStatus = 'ENABLED' | 'DISABLED';

export interface AuthCenterUserProfile {
    id: number;
    username: string;
    nickname?: string;
    email?: string;
    phone?: string;
    role: AuthCenterUserRole;
    status: AuthCenterUserStatus;
    lastLoginAt?: string;
    createdAt?: string;
}

export interface AuthCenterLoginPayload {
    username: string;
    password: string;
}

export interface AuthCenterRegisterPayload {
    username: string;
    password: string;
    nickname?: string;
    email?: string;
    phone?: string;
}

export interface AuthCenterLoginData {
    token: string;
    tokenType: string;
    expiresIn: number;
    user: AuthCenterUserProfile;
}
