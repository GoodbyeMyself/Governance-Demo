import {
    getToken,
    httpRequest,
    type AuthStorageOptions,
} from '@governance/utils';
import type { ApiResponse } from '../core';
import type {
    AuthCenterCaptchaData,
    AuthCenterLoginData,
    AuthCenterLoginPayload,
    AuthCenterProfileUpdatePayload,
    AuthCenterRegisterPayload,
    AuthCenterResetPasswordPayload,
    AuthCenterSendEmailCodeData,
    AuthCenterSendEmailCodePayload,
    AuthCenterUserProfile,
} from './types';

const AUTH_API_PREFIX = '/api/auth-center';

const normalizeToken = (token?: string | null) => {
    if (!token) {
        return null;
    }

    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
};

const withAuthStorageOptions = (options?: AuthStorageOptions) => {
    if (!options) {
        return {};
    }

    const token = normalizeToken(getToken(options));
    if (!token) {
        return {
            skipAuth: true as const,
        };
    }

    return {
        skipAuth: true as const,
        customHeaders: {
            Authorization: token,
        },
    };
};

export const fetchCaptcha = async () =>
    httpRequest<ApiResponse<AuthCenterCaptchaData>>(
        `${AUTH_API_PREFIX}/captcha`,
        {
            method: 'GET',
            skipAuth: true,
        },
    );

export const sendEmailVerificationCode = async (
    payload: AuthCenterSendEmailCodePayload,
) =>
    httpRequest<ApiResponse<AuthCenterSendEmailCodeData>>(
        `${AUTH_API_PREFIX}/email-codes/send`,
        {
            method: 'POST',
            requireBody: true,
            skipAuth: true,
            data: payload,
        },
    );

export const login = async (payload: AuthCenterLoginPayload) =>
    httpRequest<ApiResponse<AuthCenterLoginData>>(`${AUTH_API_PREFIX}/login`, {
        method: 'POST',
        requireBody: true,
        skipAuth: true,
        data: payload,
    });

export const register = async (payload: AuthCenterRegisterPayload) =>
    httpRequest<ApiResponse<AuthCenterUserProfile>>(
        `${AUTH_API_PREFIX}/register`,
        {
            method: 'POST',
            requireBody: true,
            skipAuth: true,
            data: payload,
        },
    );

export const resetPassword = async (payload: AuthCenterResetPasswordPayload) =>
    httpRequest<ApiResponse<null>>(`${AUTH_API_PREFIX}/password/reset`, {
        method: 'POST',
        requireBody: true,
        skipAuth: true,
        data: payload,
    });

export const fetchCurrentUser = async (options?: AuthStorageOptions) =>
    httpRequest<ApiResponse<AuthCenterUserProfile>>(`${AUTH_API_PREFIX}/me`, {
        method: 'GET',
        ...withAuthStorageOptions(options),
    });

export const updateCurrentUserProfile = async (
    payload: AuthCenterProfileUpdatePayload,
    options?: AuthStorageOptions,
) =>
    httpRequest<ApiResponse<AuthCenterLoginData>>(
        `${AUTH_API_PREFIX}/me/profile`,
        {
            method: 'PUT',
            requireBody: true,
            data: payload,
            ...withAuthStorageOptions(options),
        },
    );

export const logout = async (options?: AuthStorageOptions) =>
    httpRequest<ApiResponse<null>>(`${AUTH_API_PREFIX}/logout`, {
        method: 'POST',
        ...withAuthStorageOptions(options),
    });
