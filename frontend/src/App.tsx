import GovernanceAssistant from '@/components/GovernanceAssistant';
import LoginPage from '@/pages/Auth/Login';
import AccessPage from '@/pages/Access';
import DataSourcePage from '@/pages/DataSource';
import HomePage from '@/pages/Home';
import MetadataCollectionDetailPage from '@/pages/MetadataCollection/Detail';
import MetadataCollectionPage from '@/pages/MetadataCollection';
import ProfilePage from '@/pages/Profile';
import UserManagementPage from '@/pages/UserManagement';
import { logout as logoutRequest } from '@/services/authCenter';
import { clearAuthState, getStoredUser, hasToken } from '@/utils/auth';
import { UserOutlined } from '@ant-design/icons';
import { Avatar, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useMemo } from 'react';
import {
    Navigate,
    Outlet,
    Route,
    Routes,
    useLocation,
    useNavigate,
} from 'react-router-dom';

const LOGIN_PATH = '/login';
const PROFILE_PATH = '/profile';

const { Header, Content } = Layout;

const buildLoginRedirect = (pathname: string, search: string) => {
    const redirect = encodeURIComponent(`${pathname}${search}`);
    return `${LOGIN_PATH}?redirect=${redirect}`;
};

const getSelectedMenuKey = (pathname: string): string | null => {
    if (pathname.startsWith('/home')) return '/home';
    if (pathname.startsWith('/data-source')) return '/data-source';
    if (pathname.startsWith('/metadata-collection')) return '/metadata-collection';
    if (pathname.startsWith('/user-management')) return '/user-management';
    if (pathname.startsWith('/access')) return '/access';
    return null;
};

const RequireAuth: React.FC = () => {
    const location = useLocation();

    if (!hasToken()) {
        return (
            <Navigate
                to={buildLoginRedirect(location.pathname, location.search)}
                replace
            />
        );
    }

    return <AppShell />;
};

const RequireAdmin: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const currentUser = getStoredUser();

    if (currentUser?.role !== 'ADMIN') {
        return <Navigate to="/home" replace />;
    }

    return <>{children}</>;
};

const AppShell: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const currentUser = getStoredUser();
    const isAdmin = currentUser?.role === 'ADMIN';
    const displayName = currentUser?.nickname?.trim() || currentUser?.username || '当前用户';
    const selectedMenuKey = getSelectedMenuKey(location.pathname);

    const menuItems = useMemo<MenuProps['items']>(() => {
        const items: NonNullable<MenuProps['items']> = [
            { key: '/home', label: '系统工作台' },
            { key: '/data-source', label: '数据源管理' },
            { key: '/metadata-collection', label: '元数据采集' },
        ];

        if (isAdmin) {
            items.push({ key: '/user-management', label: '用户管理' });
        }

        items.push({ key: '/access', label: '权限演示' });

        return items;
    }, [isAdmin]);

    const userMenuProps: MenuProps = {
        items: [
            {
                key: 'profile',
                label: '个人中心',
            },
            {
                type: 'divider',
            },
            {
                key: 'logout',
                label: '退出登录',
            },
        ],
        onClick: async ({ key }) => {
            if (key === 'profile') {
                navigate(PROFILE_PATH);
                return;
            }

            if (key === 'logout') {
                try {
                    await logoutRequest();
                } catch {
                    // Ignore remote logout failures and always clear local auth state.
                } finally {
                    clearAuthState();
                    navigate(LOGIN_PATH, { replace: true });
                }
            }
        },
    };

    return (
        <>
            <Layout style={{ minHeight: '100vh' }}>
                <Header
                    style={{
                        background: '#fff',
                        borderBottom: '1px solid #f0f0f0',
                        display: 'flex',
                        alignItems: 'center',
                        paddingInline: 24,
                        gap: 24,
                    }}
                >
                    <Typography.Text
                        style={{
                            fontSize: 18,
                            fontWeight: 600,
                            cursor: 'pointer',
                            whiteSpace: 'nowrap',
                        }}
                        onClick={() => navigate('/home')}
                    >
                        数据治理平台
                    </Typography.Text>

                    <Menu
                        mode="horizontal"
                        selectedKeys={selectedMenuKey ? [selectedMenuKey] : []}
                        items={menuItems}
                        onClick={({ key }) => navigate(String(key))}
                        style={{ flex: 1, minWidth: 0, borderBottom: 'none' }}
                    />

                    <Dropdown menu={userMenuProps} trigger={['hover']} placement="bottomRight">
                        <span
                            style={{
                                display: 'inline-flex',
                                cursor: 'pointer',
                                height: 32,
                                lineHeight: '32px',
                                padding: '0 2px',
                            }}
                        >
                            <Space size={8} align="center">
                                <Avatar size={26} icon={<UserOutlined />} />
                                <Typography.Text style={{ lineHeight: '22px' }}>
                                    {displayName}
                                </Typography.Text>
                            </Space>
                        </span>
                    </Dropdown>
                </Header>

                <Content style={{ padding: '16px 24px' }}>
                    <Outlet />
                </Content>
            </Layout>

            {location.pathname !== LOGIN_PATH && <GovernanceAssistant />}
        </>
    );
};

const App: React.FC = () => {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />

            <Route element={<RequireAuth />}>
                <Route path="/" element={<Navigate to="/home" replace />} />
                <Route path="/home" element={<HomePage />} />
                <Route path="/data-source" element={<DataSourcePage />} />
                <Route path="/metadata-collection" element={<MetadataCollectionPage />} />
                <Route
                    path="/user-management"
                    element={
                        <RequireAdmin>
                            <UserManagementPage />
                        </RequireAdmin>
                    }
                />
                <Route path="/profile" element={<ProfilePage />} />
                <Route
                    path="/metadata-collection/:id"
                    element={<MetadataCollectionDetailPage />}
                />
                <Route path="/access" element={<AccessPage />} />
            </Route>

            <Route
                path="*"
                element={<Navigate to={hasToken() ? '/home' : '/login'} replace />}
            />
        </Routes>
    );
};

export default App;
