import type { ApiResponse } from './dataSource';
import { httpRequest } from '@/utils/http';

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

export interface AuthCenterUpdateUserRolePayload {
    role: AuthCenterUserRole;
}

export async function login(payload: AuthCenterLoginPayload) {
    return httpRequest<ApiResponse<AuthCenterLoginData>>('/api/auth-center/login', {
        method: 'POST',
        requireBody: true,
        skipAuth: true,
        data: payload,
    });
}

export async function register(payload: AuthCenterRegisterPayload) {
    return httpRequest<ApiResponse<AuthCenterUserProfile>>(
        '/api/auth-center/register',
        {
            method: 'POST',
            requireBody: true,
            skipAuth: true,
            data: payload,
        },
    );
}

export async function fetchCurrentUser() {
    return httpRequest<ApiResponse<AuthCenterUserProfile>>('/api/auth-center/me', {
        method: 'GET',
    });
}

export async function logout() {
    return httpRequest<ApiResponse<null>>('/api/auth-center/logout', {
        method: 'POST',
    });
}

export async function fetchAuthUsers() {
    return httpRequest<ApiResponse<AuthCenterUserProfile[]>>('/api/auth-center/users', {
        method: 'GET',
    });
}

export async function updateAuthUserRole(
    userId: number,
    payload: AuthCenterUpdateUserRolePayload,
) {
    return httpRequest<ApiResponse<AuthCenterUserProfile>>(
        `/api/auth-center/users/${userId}/role`,
        {
            method: 'PUT',
            requireBody: true,
            data: payload,
        },
    );
}
