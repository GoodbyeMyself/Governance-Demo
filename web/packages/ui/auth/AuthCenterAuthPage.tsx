import {
    LockOutlined,
    MailOutlined,
    PhoneOutlined,
    UserOutlined,
} from '@ant-design/icons';
import {
    fetchCurrentUser,
    login,
    register,
    type AuthCenterLoginPayload,
    type AuthCenterRegisterPayload,
} from '@governance/api';
import { LanguageSelect } from '@governance/components';
import { useI18n } from '@governance/i18n';
import { setAuthState, type AuthStorageOptions } from '@governance/utils';
import { Button, Card, Form, Input, Tabs, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

type LoginFormValues = AuthCenterLoginPayload;
type RegisterFormValues = AuthCenterRegisterPayload & {
    confirmPassword: string;
};

export interface AuthCenterAuthPageProps extends AuthStorageOptions {
    title?: string;
    subtitle?: string;
    defaultRedirect?: string;
}

const resolveRedirectPath = (search: string, fallback: string) => {
    const params = new URLSearchParams(search || '');
    return params.get('redirect') || fallback;
};

export const AuthCenterAuthPage: React.FC<AuthCenterAuthPageProps> = ({
    title,
    subtitle,
    defaultRedirect = '/home',
    tokenKey,
    userKey,
}) => {
    const { t } = useI18n();
    const location = useLocation();
    const navigate = useNavigate();
    const storageOptions = useMemo(
        () => ({ tokenKey, userKey }),
        [tokenKey, userKey],
    );
    const redirectPath = useMemo(
        () => resolveRedirectPath(location.search, defaultRedirect),
        [location.search, defaultRedirect],
    );

    const [activeTab, setActiveTab] = useState<'login' | 'register'>('login');
    const [loginForm] = Form.useForm<LoginFormValues>();
    const [registerForm] = Form.useForm<RegisterFormValues>();
    const [loginLoading, setLoginLoading] = useState(false);
    const [registerLoading, setRegisterLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const handleLogin = async () => {
        try {
            const values = await loginForm.validateFields();
            const payload: AuthCenterLoginPayload = {
                username: values.username.trim(),
                password: values.password,
            };

            setLoginLoading(true);
            const response = await login(payload);
            if (!response.success || !response.data?.token) {
                throw new Error(response.message || t('auth.loginFailed'));
            }

            const token = response.data.token;
            setAuthState(token, response.data.user, storageOptions);

            try {
                const meResponse = await fetchCurrentUser(storageOptions);
                if (meResponse.success && meResponse.data) {
                    setAuthState(token, meResponse.data, storageOptions);
                }
            } catch {
            }

            messageApi.success(t('auth.loginSuccess'));
            navigate(redirectPath, { replace: true });
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error ? error.message : t('auth.loginFailed');
            messageApi.error(messageText);
        } finally {
            setLoginLoading(false);
        }
    };

    const handleRegister = async () => {
        try {
            const values = await registerForm.validateFields();
            const payload: AuthCenterRegisterPayload = {
                username: values.username.trim(),
                password: values.password,
                nickname: values.nickname?.trim() || undefined,
                email: values.email?.trim() || undefined,
                phone: values.phone?.trim() || undefined,
            };

            setRegisterLoading(true);
            const response = await register(payload);
            if (!response.success) {
                throw new Error(response.message || t('auth.registerFailed'));
            }

            messageApi.success(t('auth.registerSuccess'));
            setActiveTab('login');
            loginForm.setFieldsValue({
                username: values.username.trim(),
                password: '',
            });
            registerForm.resetFields(['password', 'confirmPassword']);
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error
                    ? error.message
                    : t('auth.registerFailed');
            messageApi.error(messageText);
        } finally {
            setRegisterLoading(false);
        }
    };

    return (
        <div
            style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: 24,
                background:
                    'linear-gradient(135deg, rgba(22,119,255,0.12) 0%, rgba(82,196,26,0.08) 100%)',
                position: 'relative',
            }}
        >
            {contextHolder}

            <div
                style={{
                    position: 'absolute',
                    top: 24,
                    right: 24,
                }}
            >
                <LanguageSelect />
            </div>

            <Card
                style={{
                    width: '100%',
                    maxWidth: 440,
                    borderRadius: 16,
                    boxShadow: '0 12px 30px rgba(31, 35, 41, 0.08)',
                }}
                styles={{
                    body: {
                        padding: 28,
                    },
                }}
            >
                <div style={{ marginBottom: 20, textAlign: 'center' }}>
                    <Typography.Title level={3} style={{ marginBottom: 8 }}>
                        {title || t('common.appName')}
                    </Typography.Title>
                    <Typography.Paragraph
                        type="secondary"
                        style={{ marginBottom: 0 }}
                    >
                        {subtitle || t('auth.loginCardSubtitle')}
                    </Typography.Paragraph>
                </div>

                <Tabs
                    activeKey={activeTab}
                    onChange={(key) =>
                        setActiveTab(key as 'login' | 'register')
                    }
                    items={[
                        {
                            key: 'login',
                            label: t('auth.tab.login'),
                            children: (
                                <Form<LoginFormValues>
                                    form={loginForm}
                                    layout="vertical"
                                    onFinish={() => void handleLogin()}
                                >
                                    <Form.Item
                                        label={t('auth.username')}
                                        name="username"
                                        rules={[
                                            {
                                                required: true,
                                                message: t('auth.usernameRequired'),
                                            },
                                            {
                                                max: 100,
                                                message: t('auth.usernameMax'),
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined />}
                                            placeholder={t(
                                                'auth.usernamePlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.password')}
                                        name="password"
                                        rules={[
                                            {
                                                required: true,
                                                message: t('auth.passwordRequired'),
                                            },
                                            {
                                                max: 100,
                                                message: t('auth.passwordMax'),
                                            },
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder={t(
                                                'auth.passwordPlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        loading={loginLoading}
                                        block
                                    >
                                        {t('auth.login')}
                                    </Button>
                                </Form>
                            ),
                        },
                        {
                            key: 'register',
                            label: t('auth.tab.register'),
                            children: (
                                <Form<RegisterFormValues>
                                    form={registerForm}
                                    layout="vertical"
                                    onFinish={() => void handleRegister()}
                                >
                                    <Form.Item
                                        label={t('auth.username')}
                                        name="username"
                                        rules={[
                                            {
                                                required: true,
                                                message: t('auth.usernameRequired'),
                                            },
                                            {
                                                max: 100,
                                                message: t('auth.usernameMax'),
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined />}
                                            placeholder={t(
                                                'auth.usernamePlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.password')}
                                        name="password"
                                        rules={[
                                            {
                                                required: true,
                                                message: t('auth.passwordRequired'),
                                            },
                                            {
                                                max: 100,
                                                message: t('auth.passwordMax'),
                                            },
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder={t(
                                                'auth.passwordPlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.confirmPassword')}
                                        name="confirmPassword"
                                        dependencies={['password']}
                                        rules={[
                                            {
                                                required: true,
                                                message: t(
                                                    'auth.confirmPasswordRequired',
                                                ),
                                            },
                                            ({ getFieldValue }) => ({
                                                validator(_, value) {
                                                    if (
                                                        !value ||
                                                        getFieldValue(
                                                            'password',
                                                        ) === value
                                                    ) {
                                                        return Promise.resolve();
                                                    }

                                                    return Promise.reject(
                                                        new Error(
                                                            t(
                                                                'auth.passwordMismatch',
                                                            ),
                                                        ),
                                                    );
                                                },
                                            }),
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder={t(
                                                'auth.confirmPasswordPlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.nickname')}
                                        name="nickname"
                                        rules={[
                                            {
                                                max: 100,
                                                message: t('auth.nicknameMax'),
                                            },
                                        ]}
                                    >
                                        <Input
                                            placeholder={t(
                                                'auth.nicknamePlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.email')}
                                        name="email"
                                        rules={[
                                            {
                                                type: 'email',
                                                message: t('auth.invalidEmail'),
                                            },
                                            {
                                                max: 100,
                                                message: t('auth.emailMax'),
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<MailOutlined />}
                                            placeholder={t(
                                                'auth.emailPlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Form.Item
                                        label={t('auth.phone')}
                                        name="phone"
                                        rules={[
                                            {
                                                pattern: /^[0-9+-]{6,30}$/,
                                                message: t('auth.invalidPhone'),
                                            },
                                            {
                                                max: 30,
                                                message: t('auth.phoneMax'),
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<PhoneOutlined />}
                                            placeholder={t(
                                                'auth.phonePlaceholder',
                                            )}
                                        />
                                    </Form.Item>

                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        loading={registerLoading}
                                        block
                                    >
                                        {t('auth.register')}
                                    </Button>
                                </Form>
                            ),
                        },
                    ]}
                />
            </Card>
        </div>
    );
};
