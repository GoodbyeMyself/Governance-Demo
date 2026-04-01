export const isAdminRole = (role?: string | null) => role === 'ADMIN';

export const isAdminUser = <T extends { role?: string | null }>(
    user?: T | null,
) => isAdminRole(user?.role);
