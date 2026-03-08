import AccessPage from '@/features/access';
import LoginPage from '@/features/auth/login';
import DataSourcePage from '@/features/data-source';
import HomePage from '@/features/home';
import MetadataCollectionPage from '@/features/metadata-collection';
import MetadataCollectionDetailPage from '@/features/metadata-collection/detail';
import ProfilePage from '@/features/profile';
import UserManagementPage from '@/features/user-management';
import { useI18n } from '@governance/i18n';
import { GovernanceAppShell, RequireAdmin, RequireAuth } from '@governance/ui';
import {
    ACCESS_PATH,
    DATA_SOURCE_PATH,
    HOME_PATH,
    LOGIN_PATH,
    METADATA_COLLECTION_PATH,
    PROFILE_PATH,
    USER_MANAGEMENT_PATH,
    hasToken,
} from '@governance/utils';
import { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

const App: React.FC = () => {
    const { t } = useI18n();

    useEffect(() => {
        document.title = t('common.appName');
    }, [t]);

    return (
        <Routes>
            <Route path={LOGIN_PATH} element={<LoginPage />} />

            <Route
                element={
                    <RequireAuth>
                        <GovernanceAppShell />
                    </RequireAuth>
                }
            >
                <Route path="/" element={<Navigate to={HOME_PATH} replace />} />
                <Route path={HOME_PATH} element={<HomePage />} />
                <Route path={DATA_SOURCE_PATH} element={<DataSourcePage />} />
                <Route
                    path={METADATA_COLLECTION_PATH}
                    element={<MetadataCollectionPage />}
                />
                <Route
                    path={USER_MANAGEMENT_PATH}
                    element={
                        <RequireAdmin>
                            <UserManagementPage />
                        </RequireAdmin>
                    }
                />
                <Route path={PROFILE_PATH} element={<ProfilePage />} />
                <Route
                    path={`${METADATA_COLLECTION_PATH}/:id`}
                    element={<MetadataCollectionDetailPage />}
                />
                <Route path={ACCESS_PATH} element={<AccessPage />} />
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
