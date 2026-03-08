import LoginPage from '@/features/auth/login';
import HomePage from '@/features/home';
import { useI18n } from '@governance/i18n';
import { PortalAppShell, RequireAuth } from '@governance/ui';
import { HOME_PATH, LOGIN_PATH, hasToken } from '@governance/utils';
import { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

const App: React.FC = () => {
    const { t } = useI18n();

    useEffect(() => {
        document.title = t('common.portalName');
    }, [t]);

    return (
        <Routes>
            <Route path={LOGIN_PATH} element={<LoginPage />} />

            <Route
                element={
                    <RequireAuth>
                        <PortalAppShell />
                    </RequireAuth>
                }
            >
                <Route path="/" element={<Navigate to={HOME_PATH} replace />} />
                <Route path={HOME_PATH} element={<HomePage />} />
            </Route>

            <Route
                path="*"
                element={
                    <Navigate
                        to={hasToken() ? HOME_PATH : LOGIN_PATH}
                        replace
                    />
                }
            />
        </Routes>
    );
};

export default App;
