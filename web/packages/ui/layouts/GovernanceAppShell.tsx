import { UserOutlined } from '@ant-design/icons';
import { logout, type AuthCenterUserProfile } from '@governance/api';
import { GovernanceAssistant, LanguageSelect } from '@governance/components';
import { useI18n } from '@governance/i18n';
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
    buildPortalAppUrl,
    clearAuthState,
    getStoredUser,
    isAdminUser,
} from '@governance/utils';
import type { MenuProps } from 'antd';
import { Avatar, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import { useMemo } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';

const { Header, Content, Sider } = Layout;

const NAV_PATHS = [
    HOME_PATH,
    DATA_SOURCE_PATH,
    IOT_DEVICE_PATH,
    IOT_COLLECTION_PATH,
    METADATA_COLLECTION_PATH,
    USER_MANAGEMENT_PATH,
    ROLE_MANAGEMENT_PATH,
    ACCESS_PATH,
] as const;

const getSelectedMenuKey = (pathname: string): string | null =>
    NAV_PATHS.find((path) => pathname.startsWith(path)) || null;

export const GovernanceAppShell: React.FC = () => {
    const { t } = useI18n();
    const location = useLocation();
    const navigate = useNavigate();
    const currentUser = getStoredUser<AuthCenterUserProfile>();
    const isAdmin = isAdminUser(currentUser);
    const displayName =
        currentUser?.nickname?.trim() ||
        currentUser?.username ||
        t('common.currentUser');
    const selectedMenuKey = getSelectedMenuKey(location.pathname);

    const menuItems = useMemo<MenuProps['items']>(() => {
        const items: NonNullable<MenuProps['items']> = [
            { key: HOME_PATH, label: t('nav.home') },
            { key: DATA_SOURCE_PATH, label: t('nav.dataSource') },
            { key: IOT_DEVICE_PATH, label: t('nav.iotDevice') },
            { key: IOT_COLLECTION_PATH, label: t('nav.iotCollection') },
            {
                key: METADATA_COLLECTION_PATH,
                label: t('nav.metadataCollection'),
            },
        ];

        if (isAdmin) {
            items.push({
                key: USER_MANAGEMENT_PATH,
                label: t('nav.userManagement'),
            });
            items.push({
                key: ROLE_MANAGEMENT_PATH,
                label: t('nav.roleManagement'),
            });
        }

        items.push({ key: ACCESS_PATH, label: t('nav.access') });
        return items;
    }, [isAdmin, t]);

    const userMenuProps: MenuProps = {
        items: [
            { key: 'profile', label: t('nav.profile') },
            { key: 'portal', label: t('nav.goToPortal') },
            { type: 'divider' },
            { key: 'logout', label: t('nav.logout') },
        ],
        onClick: async ({ key }) => {
            if (key === 'profile') {
                navigate(PROFILE_PATH);
                return;
            }

            if (key === 'portal') {
                window.location.assign(buildPortalAppUrl(HOME_PATH));
                return;
            }

            if (key === 'logout') {
                try {
                    await logout();
                } catch {
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
                <Sider
                    width={220}
                    theme="light"
                    breakpoint="lg"
                    collapsedWidth={72}
                    style={{
                        borderRight: '1px solid #f0f0f0',
                    }}
                >
                    <div
                        style={{
                            height: 64,
                            display: 'flex',
                            alignItems: 'center',
                            paddingInline: 20,
                            borderBottom: '1px solid #f0f0f0',
                            cursor: 'pointer',
                        }}
                        onClick={() => navigate(HOME_PATH)}
                    >
                        <Typography.Text
                            style={{
                                fontSize: 18,
                                fontWeight: 700,
                                color: '#1677ff',
                            }}
                        >
                            Govern
                        </Typography.Text>
                    </div>

                    <Menu
                        mode="inline"
                        selectedKeys={selectedMenuKey ? [selectedMenuKey] : []}
                        items={menuItems}
                        onClick={({ key }) => navigate(String(key))}
                        style={{ borderInlineEnd: 'none', paddingTop: 8 }}
                    />
                </Sider>

                <Layout>
                    <Header
                        style={{
                            background: '#fff',
                            borderBottom: '1px solid #f0f0f0',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            paddingInline: 24,
                            gap: 16,
                        }}
                    >
                        <div>
                            <Typography.Title
                                level={5}
                                style={{ margin: 0, lineHeight: '24px' }}
                            >
                                {t('common.appName')}
                            </Typography.Title>
                        </div>

                        <Space size={12}>
                            <LanguageSelect size="small" />
                            <Dropdown
                                menu={userMenuProps}
                                trigger={['hover', 'click']}
                                placement="bottomRight"
                            >
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
                                        <Avatar
                                            size={26}
                                            icon={<UserOutlined />}
                                        />
                                        <Typography.Text
                                            style={{ lineHeight: '22px' }}
                                        >
                                            {displayName}
                                        </Typography.Text>
                                    </Space>
                                </span>
                            </Dropdown>
                        </Space>
                    </Header>

                    <Content style={{ padding: '16px 24px' }}>
                        <Outlet />
                    </Content>
                </Layout>
            </Layout>

            {location.pathname !== LOGIN_PATH && <GovernanceAssistant />}
        </>
    );
};
