import { logout, type AuthCenterUserProfile } from '@governance/api';
import { LanguageSelect } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    HOME_PATH,
    LOGIN_PATH,
    clearAuthState,
    getStoredUser,
} from '@governance/utils';
import { UserOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { Avatar, Dropdown, Layout, Space, Typography } from 'antd';
import { Outlet, useNavigate } from 'react-router-dom';

const { Header, Content } = Layout;

export const PortalAppShell: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const currentUser = getStoredUser<AuthCenterUserProfile>();
    const displayName =
        currentUser?.nickname?.trim() ||
        currentUser?.username ||
        t('common.currentUser');

    const userMenuProps: MenuProps = {
        items: [
            { key: 'logout', label: t('nav.logout') },
        ],
        onClick: async ({ key }) => {
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
        <Layout style={{ minHeight: '100vh', background: '#f5f7fb' }}>
            <Header
                style={{
                    background: '#fff',
                    borderBottom: '1px solid #f0f0f0',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    paddingInline: 24,
                }}
            >
                <Typography.Text
                    style={{
                        fontSize: 18,
                        fontWeight: 700,
                        color: '#1677ff',
                        cursor: 'pointer',
                    }}
                    onClick={() => navigate(HOME_PATH)}
                >
                    {t('common.portalName')}
                </Typography.Text>

                <Space size={12}>
                    <LanguageSelect size="small" />
                    <Dropdown
                        menu={userMenuProps}
                        trigger={['hover']}
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
                                <Avatar size={26} icon={<UserOutlined />} />
                                <Typography.Text style={{ lineHeight: '22px' }}>
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
    );
};
