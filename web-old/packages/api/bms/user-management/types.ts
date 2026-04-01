export type BmsUserRole = 'USER' | 'ADMIN';
export type BmsUserStatus = 'ENABLED' | 'DISABLED';

export interface BmsUserProfile {
    id: number;
    username: string;
    nickname?: string;
    email?: string;
    phone?: string;
    role: BmsUserRole;
    status: BmsUserStatus;
    lastLoginAt?: string;
    createdAt?: string;
}

export interface BmsUserRoleUpdatePayload {
    role: BmsUserRole;
}
