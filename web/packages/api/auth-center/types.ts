export type AuthCenterUserRole = 'USER' | 'ADMIN';
export type AuthCenterUserStatus = 'ENABLED' | 'DISABLED';
export type EmailVerificationScene = 'REGISTER' | 'RESET_PASSWORD';

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

export interface AuthCenterCaptchaData {
    captchaId: string;
    imageData: string;
    expiresIn: number;
}

export interface AuthCenterLoginPayload {
    username: string;
    password: string;
    captchaId: string;
    captchaCode: string;
}

export interface AuthCenterRegisterPayload {
    username: string;
    password: string;
    email: string;
    emailVerificationCode: string;
}

export interface AuthCenterSendEmailCodePayload {
    scene: EmailVerificationScene;
    email: string;
    captchaId: string;
    captchaCode: string;
    username?: string;
}

export interface AuthCenterSendEmailCodeData {
    expiresIn: number;
    resendIn: number;
    debugCode?: string;
}

export interface AuthCenterResetPasswordPayload {
    username: string;
    email: string;
    newPassword: string;
    emailVerificationCode: string;
}

export interface AuthCenterProfileUpdatePayload {
    username: string;
    email: string;
    phone?: string;
}

export interface AuthCenterLoginData {
    token: string;
    tokenType: string;
    expiresIn: number;
    user: AuthCenterUserProfile;
}
