import { fetchCurrentUser } from '@governance/api';
import {
    buildLoginRedirect,
    clearAuthState,
    getAuthPersistence,
    setAuthState,
} from '@governance/utils';
import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

export interface RequireAuthProps {
    children: React.ReactNode;
}

export const RequireAuth: React.FC<RequireAuthProps> = ({ children }) => {
    const location = useLocation();
    const [authResolved, setAuthResolved] = useState(false);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        let disposed = false;

        const resolveAuth = async () => {
            try {
                const response = await fetchCurrentUser({
                    silentUnauthorized: true,
                });

                if (response.success && response.data) {
                    setAuthState('', response.data, {
                        persistence: getAuthPersistence(),
                    });

                    if (!disposed) {
                        setAuthenticated(true);
                    }
                    return;
                }
            } catch {
                clearAuthState();
            } finally {
                if (!disposed) {
                    setAuthResolved(true);
                }
            }
        };

        void resolveAuth();

        return () => {
            disposed = true;
        };
    }, []);

    if (!authResolved) {
        return (
            <div
                style={{
                    minHeight: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                }}
            >
                <Spin size="large" />
            </div>
        );
    }

    if (!authenticated) {
        return (
            <Navigate
                to={buildLoginRedirect(location.pathname, location.search)}
                replace
            />
        );
    }

    return <>{children}</>;
};
