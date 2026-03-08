import { buildLoginRedirect, hasToken } from '@governance/utils';
import { Navigate, useLocation } from 'react-router-dom';

export interface RequireAuthProps {
    children: React.ReactNode;
}

export const RequireAuth: React.FC<RequireAuthProps> = ({ children }) => {
    const location = useLocation();

    if (!hasToken()) {
        return (
            <Navigate
                to={buildLoginRedirect(location.pathname, location.search)}
                replace
            />
        );
    }

    return <>{children}</>;
};
