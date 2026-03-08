import { translate } from '@governance/i18n';

type UserRoleValue = 'ADMIN' | 'USER';
type UserStatusValue = 'ENABLED' | 'DISABLED';

const userRoleColorMap: Record<UserRoleValue, string> = {
    ADMIN: 'red',
    USER: 'blue',
};

const userStatusColorMap: Record<UserStatusValue, string> = {
    ENABLED: 'success',
    DISABLED: 'default',
};

export const getUserRoleText = (role?: string | null) => {
    switch (role) {
        case 'ADMIN':
            return translate('enum.userRole.admin');
        case 'USER':
            return translate('enum.userRole.user');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return role;
    }
};

export const getUserRoleColor = (role?: string | null) => {
    if (!role) return 'default';
    return userRoleColorMap[role as UserRoleValue] || 'default';
};

export const getUserStatusText = (status?: string | null) => {
    switch (status) {
        case 'ENABLED':
            return translate('enum.userStatus.enabled');
        case 'DISABLED':
            return translate('enum.userStatus.disabled');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return status;
    }
};

export const getUserStatusColor = (status?: string | null) => {
    if (!status) return 'default';
    return userStatusColorMap[status as UserStatusValue] || 'default';
};
