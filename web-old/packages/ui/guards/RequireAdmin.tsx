import { HOME_PATH, getStoredUser, isAdminUser } from '@governance/utils';
import { Navigate } from 'react-router-dom';

export interface RequireAdminProps {
    children: React.ReactNode;
}

export const RequireAdmin: React.FC<RequireAdminProps> = ({ children }) => {
    const currentUser = getStoredUser<{ role?: string }>();

    if (!isAdminUser(currentUser)) {
        return <Navigate to={HOME_PATH} replace />;
    }

    return <>{children}</>;
};
