import type { AuthCenterUserProfile } from '@/services/authCenter';

type AccessInitialState = {
    currentUser?: AuthCenterUserProfile | null;
};

export default (initialState: AccessInitialState) => {
    const role = initialState?.currentUser?.role;
    const isAdmin = role === 'ADMIN';

    return {
        canSeeAdmin: isAdmin,
        canManageUsers: isAdmin,
    };
};
