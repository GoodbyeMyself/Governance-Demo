import AccessPage from '@/features/access';
import LoginPage from '@/features/auth/login';
import DataSourcePage from '@/features/data-source';
import HomePage from '@/features/home';
import IotCollectionPage from '@/features/iot-collection';
import IotCollectionDetailPage from '@/features/iot-collection/detail';
import IotDevicePage from '@/features/iot-device';
import IotDeviceDetailPage from '@/features/iot-device/detail';
import MetadataCollectionPage from '@/features/metadata-collection';
import MetadataCollectionDetailPage from '@/features/metadata-collection/detail';
import ProfilePage from '@/features/profile';
import RoleManagementPage from '@/features/role-management';
import UserManagementPage from '@/features/user-management';
import { useI18n } from '@governance/i18n';
import {
    GovernanceAppShell,
    RequireAdmin,
    RequireAuth,
} from '@governance/ui';
import {
    ACCESS_PATH,
    DATA_SOURCE_PATH,
    HOME_PATH,
    IOT_COLLECTION_PATH,
    IOT_DEVICE_PATH,
    LOGIN_PATH,
    METADATA_COLLECTION_PATH,
    PROFILE_PATH,
    ROLE_MANAGEMENT_PATH,
    USER_MANAGEMENT_PATH,
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
                <Route path={IOT_DEVICE_PATH} element={<IotDevicePage />} />
                <Route path={`${IOT_DEVICE_PATH}/:id`} element={<IotDeviceDetailPage />} />
                <Route path={IOT_COLLECTION_PATH} element={<IotCollectionPage />} />
                <Route path={`${IOT_COLLECTION_PATH}/:id`} element={<IotCollectionDetailPage />} />
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
                <Route
                    path={ROLE_MANAGEMENT_PATH}
                    element={
                        <RequireAdmin>
                            <RoleManagementPage />
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
                element={<Navigate to={HOME_PATH} replace />}
            />
        </Routes>
    );
};

export default App;
