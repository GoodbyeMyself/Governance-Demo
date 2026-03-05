import {
    fetchCurrentUser,
    login,
    register,
    type AuthCenterLoginPayload,
    type AuthCenterRegisterPayload,
} from '@/services/authCenter';
import { setAuthState } from '@/utils/auth';
import { LockOutlined, MailOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { history, useLocation } from '@umijs/max';
import { Button, Card, Form, Input, Tabs, message } from 'antd';
import { useMemo, useState } from 'react';
import styles from './index.less';

type LoginFormValues = AuthCenterLoginPayload;

type RegisterFormValues = AuthCenterRegisterPayload & {
    confirmPassword: string;
};

const LoginPage: React.FC = () => {
    const location = useLocation();
    const [activeTab, setActiveTab] = useState<'login' | 'register'>('login');
    const [loginForm] = Form.useForm<LoginFormValues>();
    const [registerForm] = Form.useForm<RegisterFormValues>();
    const [loginLoading, setLoginLoading] = useState(false);
    const [registerLoading, setRegisterLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const redirectPath = useMemo(() => {
        const search = new URLSearchParams(location.search || '');
        return search.get('redirect') || '/home';
    }, [location.search]);

    const handleLogin = async () => {
        try {
            const values = await loginForm.validateFields();
            const payload: AuthCenterLoginPayload = {
                username: values.username.trim(),
                password: values.password,
            };

            setLoginLoading(true);
            const res = await login(payload);
            if (!res.success || !res.data?.token) {
                throw new Error(res.message || '登录失败');
            }

            const token = res.data.token;
            setAuthState(token, res.data.user);

            try {
                const meRes = await fetchCurrentUser();
                if (meRes.success && meRes.data) {
                    setAuthState(token, meRes.data);
                }
            } catch {
                // /me 失败时不打断登录流程。
            }

            messageApi.success('登录成功');
            history.replace(redirectPath);
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }
            const msg = error instanceof Error ? error.message : '登录失败';
            messageApi.error(msg);
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
            const res = await register(payload);
            if (!res.success) {
                throw new Error(res.message || '注册失败');
            }

            messageApi.success('注册成功，请使用新账号登录');
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
            const msg = error instanceof Error ? error.message : '注册失败';
            messageApi.error(msg);
        } finally {
            setRegisterLoading(false);
        }
    };

    return (
        <div className={styles.page}>
            {contextHolder}
            <Card className={styles.card}>
                <div className={styles.title}>数据治理平台</div>
                <div className={styles.subtitle}>请先登录后再使用系统功能</div>

                <Tabs
                    activeKey={activeTab}
                    onChange={(key) => setActiveTab(key as 'login' | 'register')}
                    items={[
                        {
                            key: 'login',
                            label: '登录',
                            children: (
                                <Form<LoginFormValues>
                                    form={loginForm}
                                    layout="vertical"
                                    onFinish={() => void handleLogin()}
                                >
                                    <Form.Item
                                        label="用户名"
                                        name="username"
                                        rules={[
                                            { required: true, message: '请输入用户名' },
                                            {
                                                min: 4,
                                                max: 64,
                                                message: '用户名长度为 4-64 位',
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined />}
                                            placeholder="请输入用户名"
                                        />
                                    </Form.Item>
                                    <Form.Item
                                        label="密码"
                                        name="password"
                                        rules={[
                                            { required: true, message: '请输入密码' },
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder="请输入密码"
                                        />
                                    </Form.Item>

                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        block
                                        loading={loginLoading}
                                    >
                                        登录
                                    </Button>
                                </Form>
                            ),
                        },
                        {
                            key: 'register',
                            label: '注册',
                            children: (
                                <Form<RegisterFormValues>
                                    form={registerForm}
                                    layout="vertical"
                                    onFinish={() => void handleRegister()}
                                >
                                    <Form.Item
                                        label="用户名"
                                        name="username"
                                        rules={[
                                            { required: true, message: '请输入用户名' },
                                            {
                                                pattern: /^[a-zA-Z0-9_]+$/,
                                                message: '仅支持字母、数字和下划线',
                                            },
                                            {
                                                min: 4,
                                                max: 64,
                                                message: '用户名长度为 4-64 位',
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined />}
                                            placeholder="请输入用户名"
                                        />
                                    </Form.Item>
                                    <Form.Item
                                        label="密码"
                                        name="password"
                                        rules={[
                                            { required: true, message: '请输入密码' },
                                            {
                                                min: 8,
                                                max: 20,
                                                message: '密码长度为 8-20 位',
                                            },
                                            {
                                                pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
                                                message: '密码需同时包含字母和数字',
                                            },
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder="请输入密码"
                                        />
                                    </Form.Item>
                                    <Form.Item
                                        label="确认密码"
                                        name="confirmPassword"
                                        dependencies={['password']}
                                        rules={[
                                            { required: true, message: '请再次输入密码' },
                                            ({ getFieldValue }) => ({
                                                validator(_, value) {
                                                    if (
                                                        !value ||
                                                        getFieldValue('password') === value
                                                    ) {
                                                        return Promise.resolve();
                                                    }
                                                    return Promise.reject(
                                                        new Error('两次输入的密码不一致'),
                                                    );
                                                },
                                            }),
                                        ]}
                                    >
                                        <Input.Password
                                            prefix={<LockOutlined />}
                                            placeholder="请再次输入密码"
                                        />
                                    </Form.Item>
                                    <Form.Item
                                        label="昵称"
                                        name="nickname"
                                        rules={[
                                            {
                                                max: 100,
                                                message: '昵称最多 100 个字符',
                                            },
                                        ]}
                                    >
                                        <Input placeholder="选填" />
                                    </Form.Item>
                                    <Form.Item
                                        label="邮箱"
                                        name="email"
                                        rules={[
                                            {
                                                type: 'email',
                                                message: '邮箱格式不正确',
                                            },
                                            {
                                                max: 100,
                                                message: '邮箱最多 100 个字符',
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<MailOutlined />}
                                            placeholder="选填"
                                        />
                                    </Form.Item>
                                    <Form.Item
                                        label="手机号"
                                        name="phone"
                                        rules={[
                                            {
                                                pattern: /^[0-9+-]{6,30}$/,
                                                message: '手机号格式不正确',
                                            },
                                            {
                                                max: 30,
                                                message: '手机号最多 30 个字符',
                                            },
                                        ]}
                                    >
                                        <Input
                                            prefix={<PhoneOutlined />}
                                            placeholder="选填"
                                        />
                                    </Form.Item>

                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        block
                                        loading={registerLoading}
                                    >
                                        注册
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

export default LoginPage;
