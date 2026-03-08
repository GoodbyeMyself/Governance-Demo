import {
    LockOutlined,
    MailOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    UserOutlined,
} from '@ant-design/icons';
import {
    fetchCaptcha,
    fetchCurrentUser,
    login,
    register,
    resetPassword,
    sendEmailVerificationCode,
    type AuthCenterLoginPayload,
    type AuthCenterRegisterPayload,
    type AuthCenterResetPasswordPayload,
    type EmailVerificationScene,
} from '@governance/api';
import { LanguageSelect } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    clearRememberedLoginCredentials,
    getRememberedLoginCredentials,
    setAuthState,
    setRememberedLoginCredentials,
    type AuthStorageOptions,
} from '@governance/utils';
import {
    Button,
    Card,
    Checkbox,
    Form,
    Grid,
    Input,
    Modal,
    Space,
    Typography,
    message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

type Mode = 'login' | 'register' | 'forgot';
type LoginForm = {
    username: string;
    password: string;
    captchaCode: string;
    rememberPassword?: boolean;
    agreeAgreement?: boolean;
};
type RegisterForm = {
    username: string;
    password: string;
    confirmPassword: string;
    email: string;
    captchaCode: string;
    emailVerificationCode: string;
    agreeAgreement?: boolean;
};
type ForgotForm = {
    username: string;
    email: string;
    newPassword: string;
    confirmPassword: string;
    captchaCode: string;
    emailVerificationCode: string;
    agreeAgreement?: boolean;
};

export interface AuthCenterAuthPageProps extends AuthStorageOptions {
    title?: string;
    subtitle?: string;
    defaultRedirect?: string;
}

const resolveRedirectPath = (search: string, fallback: string) =>
    new URLSearchParams(search || '').get('redirect') || fallback;

const agreementRule = (messageText: string) => ({
    validator: async (_: unknown, value: boolean | undefined) => {
        if (!value) throw new Error(messageText);
    },
});

export const AuthCenterAuthPage: React.FC<AuthCenterAuthPageProps> = ({
    title,
    subtitle,
    defaultRedirect = '/home',
    tokenKey,
    userKey,
}) => {
    const { t } = useI18n();
    const screens = Grid.useBreakpoint();
    const location = useLocation();
    const navigate = useNavigate();
    const storageOptions = useMemo(() => ({ tokenKey, userKey }), [tokenKey, userKey]);
    const redirectPath = useMemo(
        () => resolveRedirectPath(location.search, defaultRedirect),
        [location.search, defaultRedirect],
    );
    const [mode, setMode] = useState<Mode>('login');
    const [captcha, setCaptcha] = useState<{ captchaId: string; imageData: string } | null>(null);
    const [agreementOpen, setAgreementOpen] = useState(false);
    const [loginLoading, setLoginLoading] = useState(false);
    const [registerLoading, setRegisterLoading] = useState(false);
    const [forgotLoading, setForgotLoading] = useState(false);
    const [sendingScene, setSendingScene] = useState<EmailVerificationScene | null>(null);
    const [registerCountdown, setRegisterCountdown] = useState(0);
    const [forgotCountdown, setForgotCountdown] = useState(0);
    const [loginForm] = Form.useForm<LoginForm>();
    const [registerForm] = Form.useForm<RegisterForm>();
    const [forgotForm] = Form.useForm<ForgotForm>();
    const [messageApi, contextHolder] = message.useMessage();
    const controlSize = 'middle' as const;
    const formStyle = { marginBottom: 14 };
    const twoColumnStyle = {
        display: 'grid',
        gridTemplateColumns: screens.md ? '1fr 1fr' : '1fr',
        gap: 12,
    } as const;
    const leftPanelBackground =
        "linear-gradient(180deg, rgba(15,23,42,.12), rgba(15,23,42,.36)), url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 900'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' y1='0' x2='1' y2='1'%3E%3Cstop stop-color='%230b1220'/%3E%3Cstop offset='1' stop-color='%231d4ed8'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='1200' height='900' fill='url(%23g)'/%3E%3Ccircle cx='190' cy='180' r='180' fill='rgba(96,165,250,.18)'/%3E%3Ccircle cx='980' cy='180' r='220' fill='rgba(59,130,246,.16)'/%3E%3Ccircle cx='960' cy='720' r='260' fill='rgba(37,99,235,.22)'/%3E%3Cpath d='M0 680 C220 560 360 760 600 660 C820 560 960 360 1200 470 L1200 900 L0 900 Z' fill='rgba(255,255,255,.1)'/%3E%3Cpath d='M0 760 C180 690 320 810 520 730 C760 635 900 495 1200 600 L1200 900 L0 900 Z' fill='rgba(255,255,255,.08)'/%3E%3C/svg%3E\")";
    const surfaceStyle = {
        background: 'rgba(255,255,255,.96)',
        border: '1px solid rgba(255,255,255,.58)',
        boxShadow:
            '0 28px 64px rgba(15,23,42,.18), 0 8px 24px rgba(15,23,42,.08)',
        backdropFilter: 'blur(16px)',
    } as const;
    const languageBoxStyle = {
        padding: '8px 10px',
        borderRadius: 999,
        background: 'rgba(255,255,255,.18)',
        border: '1px solid rgba(255,255,255,.18)',
        backdropFilter: 'blur(12px)',
    } as const;

    const usernameRules = [
        { required: true, message: t('auth.usernameRequired') },
        { min: 4, max: 64, message: t('auth.usernameLength') },
        { pattern: /^[a-zA-Z0-9_]+$/, message: t('auth.usernamePattern') },
    ];
    const emailRules = [
        { required: true, message: t('auth.emailRequired') },
        { type: 'email' as const, message: t('auth.invalidEmail') },
        { max: 100, message: t('auth.emailMax') },
    ];
    const passwordRules = [
        { required: true, message: t('auth.passwordRequired') },
        { min: 8, max: 20, message: t('auth.passwordLength') },
        { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: t('auth.passwordPattern') },
    ];

    const loadCaptcha = async () => {
        try {
            const response = await fetchCaptcha();
            if (!response.success || !response.data) throw new Error(response.message || t('auth.captchaLoadFailed'));
            setCaptcha({
                captchaId: response.data.captchaId,
                imageData: response.data.imageData,
            });
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('auth.captchaLoadFailed'));
        }
    };

    useEffect(() => {
        void loadCaptcha();
        const remembered = getRememberedLoginCredentials();
        if (remembered) {
            loginForm.setFieldsValue({
                username: remembered.username,
                password: remembered.password,
                rememberPassword: true,
            });
        }
    }, []);

    useEffect(() => {
        if (!registerCountdown && !forgotCountdown) return;
        const timer = window.setInterval(() => {
            setRegisterCountdown((value) => (value > 0 ? value - 1 : 0));
            setForgotCountdown((value) => (value > 0 ? value - 1 : 0));
        }, 1000);
        return () => window.clearInterval(timer);
    }, [registerCountdown, forgotCountdown]);

    const syncUser = async (token: string, persistence: 'local' | 'session') => {
        try {
            const response = await fetchCurrentUser({ ...storageOptions, persistence });
            if (response.success && response.data) {
                setAuthState(token, response.data, { ...storageOptions, persistence });
            }
        } catch {}
    };

    const saveRemember = (values: LoginForm) => {
        if (values.rememberPassword) {
            setRememberedLoginCredentials({
                username: values.username.trim(),
                password: values.password,
            });
            return;
        }
        clearRememberedLoginCredentials();
    };

    const handleLogin = async () => {
        if (!captcha) return void (await loadCaptcha());
        try {
            const values = await loginForm.validateFields();
            const persistence = values.rememberPassword ? 'local' : 'session';
            const payload: AuthCenterLoginPayload = {
                username: values.username.trim(),
                password: values.password,
                captchaId: captcha.captchaId,
                captchaCode: values.captchaCode.trim(),
            };
            setLoginLoading(true);
            const response = await login(payload);
            if (!response.success || !response.data?.token) throw new Error(response.message || t('auth.loginFailed'));
            setAuthState(response.data.token, response.data.user, { ...storageOptions, persistence });
            saveRemember(values);
            await syncUser(response.data.token, persistence);
            messageApi.success(t('auth.loginSuccess'));
            navigate(redirectPath, { replace: true });
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) return;
            messageApi.error(error instanceof Error ? error.message : t('auth.loginFailed'));
        } finally {
            setLoginLoading(false);
            loginForm.setFieldValue('captchaCode', '');
            await loadCaptcha();
        }
    };

    const handleRegister = async () => {
        try {
            const values = await registerForm.validateFields();
            const payload: AuthCenterRegisterPayload = {
                username: values.username.trim(),
                password: values.password,
                email: values.email.trim(),
                emailVerificationCode: values.emailVerificationCode.trim(),
            };
            setRegisterLoading(true);
            const response = await register(payload);
            if (!response.success) throw new Error(response.message || t('auth.registerFailed'));
            messageApi.success(t('auth.registerSuccess'));
            setMode('login');
            setRegisterCountdown(0);
            loginForm.setFieldsValue({ username: values.username.trim(), password: '' });
            registerForm.resetFields();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) return;
            messageApi.error(error instanceof Error ? error.message : t('auth.registerFailed'));
        } finally {
            setRegisterLoading(false);
        }
    };

    const handleForgot = async () => {
        try {
            const values = await forgotForm.validateFields();
            const payload: AuthCenterResetPasswordPayload = {
                username: values.username.trim(),
                email: values.email.trim(),
                newPassword: values.newPassword,
                emailVerificationCode: values.emailVerificationCode.trim(),
            };
            setForgotLoading(true);
            const response = await resetPassword(payload);
            if (!response.success) throw new Error(response.message || t('auth.resetPasswordFailed'));
            messageApi.success(t('auth.resetPasswordSuccess'));
            setMode('login');
            setForgotCountdown(0);
            loginForm.setFieldsValue({ username: values.username.trim(), password: '' });
            forgotForm.resetFields();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) return;
            messageApi.error(error instanceof Error ? error.message : t('auth.resetPasswordFailed'));
        } finally {
            setForgotLoading(false);
        }
    };

    const sendEmailCode = async (scene: EmailVerificationScene) => {
        if (!captcha) return void (await loadCaptcha());
        const form = scene === 'REGISTER' ? registerForm : forgotForm;
        const fields = scene === 'REGISTER' ? ['email', 'captchaCode'] : ['username', 'email', 'captchaCode'];
        try {
            const values = await form.validateFields(fields);
            setSendingScene(scene);
            const response = await sendEmailVerificationCode({
                scene,
                email: values.email.trim(),
                username: values.username?.trim(),
                captchaId: captcha.captchaId,
                captchaCode: values.captchaCode.trim(),
            });
            if (!response.success || !response.data) throw new Error(response.message || t('auth.sendEmailCodeFailed'));
            if (response.data.debugCode) {
                messageApi.info(t('auth.emailCodeDebug', { code: response.data.debugCode }));
            }
            scene === 'REGISTER'
                ? setRegisterCountdown(response.data.resendIn || 60)
                : setForgotCountdown(response.data.resendIn || 60);
            messageApi.success(t('auth.sendEmailCodeSuccess'));
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) return;
            messageApi.error(error instanceof Error ? error.message : t('auth.sendEmailCodeFailed'));
        } finally {
            setSendingScene(null);
            form.setFieldValue('captchaCode', '');
            await loadCaptcha();
        }
    };

    const agreementLabel = (
        <Typography.Text style={{ fontSize: 13 }}>
            {t('auth.agreementPrefix')}
            <Typography.Link onClick={() => setAgreementOpen(true)}>{t('auth.agreementLink')}</Typography.Link>
            {t('auth.agreementAnd')}
            <Typography.Link onClick={() => setAgreementOpen(true)}>{t('auth.privacyLink')}</Typography.Link>
        </Typography.Text>
    );

    const captchaInput = (name: string) => (
        <Space.Compact style={{ width: '100%' }}>
            <Form.Item name={name} noStyle rules={[{ required: true, message: t('auth.captchaRequired') }]}>
                <Input size={controlSize} maxLength={6} prefix={<SafetyCertificateOutlined />} placeholder={t('auth.captchaPlaceholder')} />
            </Form.Item>
            <Button size={controlSize} style={{ width: 132 }} onClick={() => void loadCaptcha()} icon={<ReloadOutlined />}>
                {captcha ? <img src={captcha.imageData} alt="captcha" style={{ width: 98, height: 32 }} /> : t('auth.captcha')}
            </Button>
        </Space.Compact>
    );

    const confirmRule = (field: 'password' | 'newPassword') => ({ getFieldValue }: { getFieldValue: (name: string) => string }) => ({
        validator(_: unknown, value: string) {
            if (!value || getFieldValue(field) === value) return Promise.resolve();
            return Promise.reject(new Error(t('auth.passwordMismatch')));
        },
    });

    const renderLogin = () => (
        <Form form={loginForm} layout="vertical" requiredMark={false}>
            <Form.Item name="username" label={t('auth.username')} rules={[{ required: true, message: t('auth.usernameRequired') }]} style={formStyle}>
                <Input size={controlSize} prefix={<UserOutlined />} placeholder={t('auth.usernamePlaceholder')} />
            </Form.Item>
            <Form.Item name="password" label={t('auth.password')} rules={[{ required: true, message: t('auth.passwordRequired') }]} style={formStyle}>
                <Input.Password size={controlSize} prefix={<LockOutlined />} placeholder={t('auth.passwordPlaceholder')} />
            </Form.Item>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
                <Form.Item name="rememberPassword" valuePropName="checked" noStyle><Checkbox>{t('auth.rememberPassword')}</Checkbox></Form.Item>
                <Typography.Link onClick={() => setMode('forgot')}>{t('auth.forgotPassword')}</Typography.Link>
            </div>
            <Form.Item name="agreeAgreement" valuePropName="checked" rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}>
                <Checkbox>{agreementLabel}</Checkbox>
            </Form.Item>
            <Button type="primary" size={controlSize} block loading={loginLoading} onClick={() => void handleLogin()}>{t('auth.login')}</Button>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
                <Typography.Text type="secondary">{t('auth.noAccount')}</Typography.Text>
                <Typography.Link style={{ marginLeft: 8 }} onClick={() => setMode('register')}>{t('auth.goRegister')}</Typography.Link>
            </div>
        </Form>
    );

    const renderRegister = () => (
        <Form form={registerForm} layout="vertical" requiredMark={false}>
            <div style={twoColumnStyle}>
                <Form.Item name="username" label={t('auth.username')} rules={usernameRules} style={formStyle}><Input size={controlSize} prefix={<UserOutlined />} placeholder={t('auth.usernamePlaceholder')} /></Form.Item>
                <Form.Item name="email" label={t('auth.email')} rules={emailRules} style={formStyle}><Input size={controlSize} prefix={<MailOutlined />} placeholder={t('auth.emailPlaceholder')} /></Form.Item>
            </div>
            <div style={twoColumnStyle}>
                <Form.Item name="password" label={t('auth.password')} rules={passwordRules} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined />} placeholder={t('auth.passwordPlaceholder')} /></Form.Item>
                <Form.Item name="confirmPassword" label={t('auth.confirmPassword')} dependencies={['password']} rules={[{ required: true, message: t('auth.confirmPasswordRequired') }, confirmRule('password')]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined />} placeholder={t('auth.confirmPasswordPlaceholder')} /></Form.Item>
            </div>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <Form.Item name="emailVerificationCode" label={t('auth.emailVerificationCode')} rules={[{ required: true, message: t('auth.emailVerificationCodeRequired') }]} style={formStyle}>
                <Space.Compact style={{ width: '100%' }}>
                    <Input size={controlSize} maxLength={6} prefix={<MailOutlined />} placeholder={t('auth.emailVerificationCodePlaceholder')} />
                    <Button size={controlSize} style={{ width: 156 }} loading={sendingScene === 'REGISTER'} disabled={registerCountdown > 0} onClick={() => void sendEmailCode('REGISTER')}>
                        {registerCountdown > 0 ? t('auth.resendCountdown', { seconds: registerCountdown }) : t('auth.sendEmailCode')}
                    </Button>
                </Space.Compact>
            </Form.Item>
            <Form.Item name="agreeAgreement" valuePropName="checked" rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}><Checkbox>{agreementLabel}</Checkbox></Form.Item>
            <Button type="primary" size={controlSize} block loading={registerLoading} onClick={() => void handleRegister()}>{t('auth.register')}</Button>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
                <Typography.Text type="secondary">{t('auth.hasAccount')}</Typography.Text>
                <Typography.Link style={{ marginLeft: 8 }} onClick={() => setMode('login')}>{t('auth.goLogin')}</Typography.Link>
            </div>
        </Form>
    );

    const renderForgot = () => (
        <Form form={forgotForm} layout="vertical" requiredMark={false}>
            <div style={twoColumnStyle}>
                <Form.Item name="username" label={t('auth.username')} rules={usernameRules} style={formStyle}><Input size={controlSize} prefix={<UserOutlined />} placeholder={t('auth.usernamePlaceholder')} /></Form.Item>
                <Form.Item name="email" label={t('auth.email')} rules={emailRules} style={formStyle}><Input size={controlSize} prefix={<MailOutlined />} placeholder={t('auth.emailPlaceholder')} /></Form.Item>
            </div>
            <div style={twoColumnStyle}>
                <Form.Item name="newPassword" label={t('auth.newPassword')} rules={[{ required: true, message: t('auth.newPasswordRequired') }, ...passwordRules.slice(1)]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined />} placeholder={t('auth.newPasswordPlaceholder')} /></Form.Item>
                <Form.Item name="confirmPassword" label={t('auth.confirmPassword')} dependencies={['newPassword']} rules={[{ required: true, message: t('auth.confirmPasswordRequired') }, confirmRule('newPassword')]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined />} placeholder={t('auth.confirmPasswordPlaceholder')} /></Form.Item>
            </div>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <Form.Item name="emailVerificationCode" label={t('auth.emailVerificationCode')} rules={[{ required: true, message: t('auth.emailVerificationCodeRequired') }]} style={formStyle}>
                <Space.Compact style={{ width: '100%' }}>
                    <Input size={controlSize} maxLength={6} prefix={<MailOutlined />} placeholder={t('auth.emailVerificationCodePlaceholder')} />
                    <Button size={controlSize} style={{ width: 156 }} loading={sendingScene === 'RESET_PASSWORD'} disabled={forgotCountdown > 0} onClick={() => void sendEmailCode('RESET_PASSWORD')}>
                        {forgotCountdown > 0 ? t('auth.resendCountdown', { seconds: forgotCountdown }) : t('auth.sendEmailCode')}
                    </Button>
                </Space.Compact>
            </Form.Item>
            <Form.Item name="agreeAgreement" valuePropName="checked" rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}><Checkbox>{agreementLabel}</Checkbox></Form.Item>
            <Button type="primary" size={controlSize} block loading={forgotLoading} onClick={() => void handleForgot()}>{t('auth.resetPassword')}</Button>
            <div style={{ textAlign: 'center', marginTop: 12 }}>
                <Typography.Text type="secondary">{t('auth.hasAccount')}</Typography.Text>
                <Typography.Link style={{ marginLeft: 8 }} onClick={() => setMode('login')}>{t('auth.goLogin')}</Typography.Link>
            </div>
        </Form>
    );

    const cardTitle = title || (mode === 'login' ? t('auth.loginCardTitle') : mode === 'register' ? t('auth.registerCardTitle') : t('auth.resetPasswordCardTitle'));
    const cardSubtitle = subtitle || (mode === 'login' ? t('auth.loginCardSubtitle') : mode === 'register' ? t('auth.registerCardSubtitle') : t('auth.resetPasswordCardSubtitle'));

    return (
        <div style={{ height: '100vh', overflow: 'hidden', background: 'radial-gradient(circle at top left, rgba(96,165,250,.22), transparent 26%), linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%)', position: 'relative' }}>
            {contextHolder}
            <div style={{ position: 'absolute', top: 12, right: 16, zIndex: 2 }}>
                <div style={languageBoxStyle}>
                    <LanguageSelect size="small" />
                </div>
            </div>
            <div style={{ maxWidth: 1240, height: '100%', margin: '0 auto', display: 'grid', gridTemplateColumns: screens.lg ? '1.08fr .92fr' : '1fr', gap: 20, alignItems: 'center' }}>
                {screens.lg && (
                    <div
                        style={{
                            height: 'calc(100vh - 240px)',
                            minHeight: 400,
                            borderRadius: 28,
                            backgroundImage: leftPanelBackground,
                            backgroundSize: 'cover',
                            backgroundPosition: 'center',
                            border: '1px solid rgba(255,255,255,.14)',
                            boxShadow: '0 30px 60px rgba(15,23,42,.24)',
                            position: 'relative',
                            overflow: 'hidden',
                        }}
                    >
                        <div
                            style={{
                                position: 'absolute',
                                inset: 20,
                                borderRadius: 22,
                                border: '1px solid rgba(255,255,255,.12)',
                                background:
                                    'linear-gradient(180deg, rgba(255,255,255,.04), rgba(255,255,255,.01))',
                            }}
                        />
                    </div>
                )}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Card
                        style={{
                            width: '100%',
                            maxWidth: 520,
                            borderRadius: 26,
                            ...surfaceStyle,
                        }}
                        styles={{ body: { padding: screens.md ? 28 : 20 } }}
                    >
                        <div
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 12,
                                marginBottom: 18,
                            }}
                        >
                            <div
                                style={{
                                    width: 44,
                                    height: 44,
                                    borderRadius: 14,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    color: '#fff',
                                    background:
                                        'linear-gradient(135deg, #1d4ed8, #3b82f6)',
                                    boxShadow:
                                        '0 10px 18px rgba(37,99,235,.28)',
                                    flexShrink: 0,
                                }}
                            >
                                <LockOutlined />
                            </div>
                            <div style={{ minWidth: 0 }}>
                                <Typography.Text
                                    style={{
                                        display: 'block',
                                        fontSize: 12,
                                        color: '#2563eb',
                                        fontWeight: 600,
                                        letterSpacing: '.08em',
                                        textTransform: 'uppercase',
                                        marginBottom: 2,
                                    }}
                                >
                                    {t('common.appName')}
                                </Typography.Text>
                                <Typography.Title
                                    level={3}
                                    style={{ margin: 0, lineHeight: 1.2 }}
                                >
                                    {cardTitle}
                                </Typography.Title>
                            </div>
                        </div>
                        <Typography.Paragraph
                            type="secondary"
                            style={{ marginBottom: 18, lineHeight: 1.6 }}
                        >
                            {cardSubtitle}
                        </Typography.Paragraph>
                        {mode === 'login' ? renderLogin() : mode === 'register' ? renderRegister() : renderForgot()}
                    </Card>
                </div>
            </div>
            <Modal title={t('auth.agreementTitle')} open={agreementOpen} footer={<Button onClick={() => setAgreementOpen(false)}>{t('common.close')}</Button>} onCancel={() => setAgreementOpen(false)}>
                <Typography.Paragraph>{t('auth.agreementContent1')}</Typography.Paragraph>
                <Typography.Paragraph>{t('auth.agreementContent2')}</Typography.Paragraph>
                <Typography.Paragraph>{t('auth.agreementContent3')}</Typography.Paragraph>
            </Modal>
        </div>
    );
};
