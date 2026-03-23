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
    clearAuthState,
    clearRememberedLoginCredentials,
    getAuthPersistence,
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
    Spin,
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

const PRIMARY_BUTTON_STYLE = {
    height: 44,
    borderRadius: 10,
    background: '#161616',
    borderColor: '#161616',
    boxShadow: 'inset 0 1px 0 rgba(255,255,255,.08)',
    fontWeight: 600,
} as const;

const SECONDARY_BUTTON_STYLE = {
    color: '#161616',
    borderColor: '#d9d9d9',
    background: '#ffffff',
} as const;

const LINK_STYLE = {
    color: '#161616',
    fontWeight: 500,
} as const;

const LOGO_ICON = (
    <svg
        xmlns='http://www.w3.org/2000/svg'
        viewBox='0 0 24 24'
        fill='none'
        stroke='currentColor'
        strokeWidth='2'
        strokeLinecap='round'
        strokeLinejoin='round'
        className='mr-2 h-6 w-6'
        style={{ width: 24, height: 24, marginRight: 8, flexShrink: 0 }}
    >
        <path d='M15 6v12a3 3 0 1 0 3-3H6a3 3 0 1 0 3 3V6a3 3 0 1 0-3 3h12a3 3 0 1 0-3-3' />
    </svg>
);

interface InteractiveGridPatternProps {
    width?: number;
    height?: number;
    squares?: [number, number];
    style?: React.CSSProperties;
}

const InteractiveGridPattern: React.FC<InteractiveGridPatternProps> = ({
    width = 40,
    height = 40,
    squares = [24, 24],
    style,
}) => {
    const [horizontal, vertical] = squares;
    const [hoveredSquare, setHoveredSquare] = useState<number | null>(null);

    return (
        <svg
            width={width * horizontal}
            height={height * vertical}
            style={{
                position: 'absolute',
                inset: 0,
                width: '100%',
                height: '100%',
                border: '1px solid rgba(156,163,175,.3)',
                ...style,
            }}
        >
            {Array.from({ length: horizontal * vertical }).map((_, index) => {
                const x = (index % horizontal) * width;
                const y = Math.floor(index / horizontal) * height;
                const isHovered = hoveredSquare === index;

                return (
                    <rect
                        key={index}
                        x={x}
                        y={y}
                        width={width}
                        height={height}
                        style={{
                            stroke: 'rgba(156,163,175,.3)',
                            fill: isHovered ? 'rgba(209,213,219,.3)' : 'transparent',
                            transitionProperty: 'all',
                            transitionDuration: isHovered ? '100ms' : '1000ms',
                            transitionTimingFunction: 'ease-in-out',
                        }}
                        onMouseEnter={() => setHoveredSquare(index)}
                        onMouseLeave={() => setHoveredSquare(null)}
                    />
                );
            })}
        </svg>
    );
};
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
    const [checkingSession, setCheckingSession] = useState(true);
    const controlSize = 'middle' as const;
    const formStyle = { marginBottom: 14 };
    const twoColumnStyle = {
        display: 'grid',
        gridTemplateColumns: screens.md ? '1fr 1fr' : '1fr',
        gap: 12,
    } as const;
    const authSurfaceStyle = {
        background: '#f7f7f5',
    } as const;
    const languageBoxStyle = {
        padding: '8px 10px',
        borderRadius: 999,
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
        let disposed = false;

        const bootstrap = async () => {
            const remembered = getRememberedLoginCredentials();
            if (remembered) {
                loginForm.setFieldsValue({
                    username: remembered.username,
                    password: remembered.password,
                    rememberPassword: true,
                });
            }

            try {
                const response = await fetchCurrentUser({
                    ...storageOptions,
                    silentUnauthorized: true,
                });

                if (response.success && response.data) {
                    setAuthState('', response.data, {
                        ...storageOptions,
                        persistence: getAuthPersistence(storageOptions),
                    });
                    navigate(redirectPath, { replace: true });
                    return;
                }
            } catch {
                clearAuthState();
            }

            await loadCaptcha();
            if (!disposed) {
                setCheckingSession(false);
            }
        };

        void bootstrap();

        return () => {
            disposed = true;
        };
    }, [loginForm, navigate, redirectPath, storageOptions]);

    useEffect(() => {
        if (!registerCountdown && !forgotCountdown) return;
        const timer = window.setInterval(() => {
            setRegisterCountdown((value) => (value > 0 ? value - 1 : 0));
            setForgotCountdown((value) => (value > 0 ? value - 1 : 0));
        }, 1000);
        return () => window.clearInterval(timer);
    }, [registerCountdown, forgotCountdown]);

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
            if (!response.success || !response.data?.user) throw new Error(response.message || t('auth.loginFailed'));
            setAuthState('', response.data.user, { ...storageOptions, persistence });
            saveRemember(values);
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
        <Typography.Text style={{ fontSize: 13, color: '#525252' }}>
            {t('auth.agreementPrefix')}
            <Typography.Link style={LINK_STYLE} onClick={() => setAgreementOpen(true)}>
                {t('auth.agreementLink')}
            </Typography.Link>
            {t('auth.agreementAnd')}
            <Typography.Link style={LINK_STYLE} onClick={() => setAgreementOpen(true)}>
                {t('auth.privacyLink')}
            </Typography.Link>
        </Typography.Text>
    );

    const captchaInput = (name: string) => (
        <Space.Compact style={{ width: '100%' }}>
            <Form.Item name={name} noStyle rules={[{ required: true, message: t('auth.captchaRequired') }]}>
                <Input
                    size={controlSize}
                    maxLength={6}
                    prefix={<SafetyCertificateOutlined style={{ color: '#525252' }} />}
                    placeholder={t('auth.captchaPlaceholder')}
                />
            </Form.Item>
            <button
                type='button'
                style={{
                    width: 132,
                    height: 32,
                    padding: 0,
                    margin: 0,
                    border: '1px solid #d9d9d9',
                    borderLeft: 'none',
                    background: '#ffffff',
                    overflow: 'hidden',
                    borderRadius: 0,
                    cursor: 'pointer',
                    display: 'block',
                    lineHeight: 0,
                    flexShrink: 0,
                }}
                onClick={() => void loadCaptcha()}
            >
                {captcha ? (
                    <img
                        src={captcha.imageData}
                        alt='captcha'
                        style={{ width: '100%', height: '100%', display: 'block', objectFit: 'cover', cursor: 'pointer' }}
                    />
                ) : (
                    <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%', lineHeight: 1.2 }}>
                        {t('auth.captcha')}
                    </span>
                )}
            </button>
        </Space.Compact>
    );

    const confirmRule = (field: 'password' | 'newPassword') => ({ getFieldValue }: { getFieldValue: (name: string) => string }) => ({
        validator(_: unknown, value: string) {
            if (!value || getFieldValue(field) === value) return Promise.resolve();
            return Promise.reject(new Error(t('auth.passwordMismatch')));
        },
    });

    const renderLogin = () => (
        <Form form={loginForm} layout='vertical' requiredMark={false}>
            <Form.Item name='username' label={t('auth.username')} rules={[{ required: true, message: t('auth.usernameRequired') }]} style={formStyle}>
                <Input size={controlSize} prefix={<UserOutlined style={{ color: '#525252' }} />} placeholder={t('auth.usernamePlaceholder')} />
            </Form.Item>
            <Form.Item name='password' label={t('auth.password')} rules={[{ required: true, message: t('auth.passwordRequired') }]} style={formStyle}>
                <Input.Password size={controlSize} prefix={<LockOutlined style={{ color: '#525252' }} />} placeholder={t('auth.passwordPlaceholder')} />
            </Form.Item>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10, gap: 12 }}>
                <Form.Item name='rememberPassword' valuePropName='checked' noStyle><Checkbox>{t('auth.rememberPassword')}</Checkbox></Form.Item>
                <Typography.Link style={LINK_STYLE} onClick={() => setMode('forgot')}>{t('auth.forgotPassword')}</Typography.Link>
            </div>
            <Form.Item name='agreeAgreement' valuePropName='checked' rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}>
                <Checkbox>{agreementLabel}</Checkbox>
            </Form.Item>
            <Button type='primary' size={controlSize} block loading={loginLoading} style={PRIMARY_BUTTON_STYLE} onClick={() => void handleLogin()}>{t('auth.login')}</Button>
            <div style={{ textAlign: 'center', marginTop: 14 }}>
                <Typography.Text type='secondary'>{t('auth.noAccount')}</Typography.Text>
                <Typography.Link style={{ ...LINK_STYLE, marginLeft: 8 }} onClick={() => setMode('register')}>{t('auth.goRegister')}</Typography.Link>
            </div>
        </Form>
    );

    const renderRegister = () => (
        <Form form={registerForm} layout='vertical' requiredMark={false}>
            <div style={twoColumnStyle}>
                <Form.Item name='username' label={t('auth.username')} rules={usernameRules} style={formStyle}><Input size={controlSize} prefix={<UserOutlined style={{ color: '#525252' }} />} placeholder={t('auth.usernamePlaceholder')} /></Form.Item>
                <Form.Item name='email' label={t('auth.email')} rules={emailRules} style={formStyle}><Input size={controlSize} prefix={<MailOutlined style={{ color: '#525252' }} />} placeholder={t('auth.emailPlaceholder')} /></Form.Item>
            </div>
            <div style={twoColumnStyle}>
                <Form.Item name='password' label={t('auth.password')} rules={passwordRules} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined style={{ color: '#525252' }} />} placeholder={t('auth.passwordPlaceholder')} /></Form.Item>
                <Form.Item name='confirmPassword' label={t('auth.confirmPassword')} dependencies={['password']} rules={[{ required: true, message: t('auth.confirmPasswordRequired') }, confirmRule('password')]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined style={{ color: '#525252' }} />} placeholder={t('auth.confirmPasswordPlaceholder')} /></Form.Item>
            </div>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <Form.Item name='emailVerificationCode' label={t('auth.emailVerificationCode')} rules={[{ required: true, message: t('auth.emailVerificationCodeRequired') }]} style={formStyle}>
                <Space.Compact style={{ width: '100%' }}>
                    <Input size={controlSize} maxLength={6} prefix={<MailOutlined style={{ color: '#525252' }} />} placeholder={t('auth.emailVerificationCodePlaceholder')} />
                    <Button size={controlSize} style={{ width: 156, ...SECONDARY_BUTTON_STYLE }} loading={sendingScene === 'REGISTER'} disabled={registerCountdown > 0} onClick={() => void sendEmailCode('REGISTER')}>
                        {registerCountdown > 0 ? t('auth.resendCountdown', { seconds: registerCountdown }) : t('auth.sendEmailCode')}
                    </Button>
                </Space.Compact>
            </Form.Item>
            <Form.Item name='agreeAgreement' valuePropName='checked' rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}><Checkbox>{agreementLabel}</Checkbox></Form.Item>
            <Button type='primary' size={controlSize} block loading={registerLoading} style={PRIMARY_BUTTON_STYLE} onClick={() => void handleRegister()}>{t('auth.register')}</Button>
            <div style={{ textAlign: 'center', marginTop: 14 }}>
                <Typography.Text type='secondary'>{t('auth.hasAccount')}</Typography.Text>
                <Typography.Link style={{ ...LINK_STYLE, marginLeft: 8 }} onClick={() => setMode('login')}>{t('auth.goLogin')}</Typography.Link>
            </div>
        </Form>
    );

    const renderForgot = () => (
        <Form form={forgotForm} layout='vertical' requiredMark={false}>
            <div style={twoColumnStyle}>
                <Form.Item name='username' label={t('auth.username')} rules={usernameRules} style={formStyle}><Input size={controlSize} prefix={<UserOutlined style={{ color: '#525252' }} />} placeholder={t('auth.usernamePlaceholder')} /></Form.Item>
                <Form.Item name='email' label={t('auth.email')} rules={emailRules} style={formStyle}><Input size={controlSize} prefix={<MailOutlined style={{ color: '#525252' }} />} placeholder={t('auth.emailPlaceholder')} /></Form.Item>
            </div>
            <div style={twoColumnStyle}>
                <Form.Item name='newPassword' label={t('auth.newPassword')} rules={[{ required: true, message: t('auth.newPasswordRequired') }, ...passwordRules.slice(1)]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined style={{ color: '#525252' }} />} placeholder={t('auth.newPasswordPlaceholder')} /></Form.Item>
                <Form.Item name='confirmPassword' label={t('auth.confirmPassword')} dependencies={['newPassword']} rules={[{ required: true, message: t('auth.confirmPasswordRequired') }, confirmRule('newPassword')]} style={formStyle}><Input.Password size={controlSize} prefix={<LockOutlined style={{ color: '#525252' }} />} placeholder={t('auth.confirmPasswordPlaceholder')} /></Form.Item>
            </div>
            <Form.Item label={t('auth.captcha')} style={formStyle}>{captchaInput('captchaCode')}</Form.Item>
            <Form.Item name='emailVerificationCode' label={t('auth.emailVerificationCode')} rules={[{ required: true, message: t('auth.emailVerificationCodeRequired') }]} style={formStyle}>
                <Space.Compact style={{ width: '100%' }}>
                    <Input size={controlSize} maxLength={6} prefix={<MailOutlined style={{ color: '#525252' }} />} placeholder={t('auth.emailVerificationCodePlaceholder')} />
                    <Button size={controlSize} style={{ width: 156, ...SECONDARY_BUTTON_STYLE }} loading={sendingScene === 'RESET_PASSWORD'} disabled={forgotCountdown > 0} onClick={() => void sendEmailCode('RESET_PASSWORD')}>
                        {forgotCountdown > 0 ? t('auth.resendCountdown', { seconds: forgotCountdown }) : t('auth.sendEmailCode')}
                    </Button>
                </Space.Compact>
            </Form.Item>
            <Form.Item name='agreeAgreement' valuePropName='checked' rules={[agreementRule(t('auth.agreementRequired'))]} style={{ marginBottom: 14 }}><Checkbox>{agreementLabel}</Checkbox></Form.Item>
            <Button type='primary' size={controlSize} block loading={forgotLoading} style={PRIMARY_BUTTON_STYLE} onClick={() => void handleForgot()}>{t('auth.resetPassword')}</Button>
            <div style={{ textAlign: 'center', marginTop: 14 }}>
                <Typography.Text type='secondary'>{t('auth.hasAccount')}</Typography.Text>
                <Typography.Link style={{ ...LINK_STYLE, marginLeft: 8 }} onClick={() => setMode('login')}>{t('auth.goLogin')}</Typography.Link>
            </div>
        </Form>
    );

    const cardTitle = title || (mode === 'login' ? t('auth.loginCardTitle') : mode === 'register' ? t('auth.registerCardTitle') : t('auth.resetPasswordCardTitle'));
    const cardSubtitle = subtitle || (mode === 'login' ? t('auth.loginCardSubtitle') : mode === 'register' ? t('auth.registerCardSubtitle') : t('auth.resetPasswordCardSubtitle'));
    const displayTitle = mode === 'login' ? 'Sign in to Govern Dashboard' : cardTitle;
    const displaySubtitle = mode === 'login' ? 'Welcome back! Please sign in to continue' : cardSubtitle;

    return (
        <div style={{ minHeight: '100vh', background: '#f7f7f5', position: 'relative' }}>
            {contextHolder}
            <div style={{ position: 'absolute', top: 12, right: 16, zIndex: 3 }}>
                <div style={languageBoxStyle}>
                    <LanguageSelect size='small' />
                </div>
            </div>
            <div
                style={{
                    height: '100vh',
                    display: 'grid',
                    gridTemplateColumns: screens.lg ? 'minmax(420px, 1fr) minmax(420px, 1fr)' : '1fr',
                }}
            >
                {screens.lg && (
                    <div
                        style={{
                            position: 'relative',
                            overflow: 'hidden',
                            background: '#18181b',
                            color: '#fafaf9',
                            padding: '40px 36px 28px',
                            display: 'flex',
                            flexDirection: 'column',
                            height: 'calc(100vh - 68px)',
                        }}
                    >
                        <div
                            style={{
                                position: 'relative',
                                zIndex: 2,
                                display: 'flex',
                                alignItems: 'center',
                                fontSize: 18,
                                fontWeight: 600,
                                marginTop: -2,
                            }}
                        >
                            {LOGO_ICON}
                            Govern Dashboard
                        </div>
                        <div
                            style={{
                                position: 'absolute',
                                inset: 0,
                                background: '#18181b',
                                zIndex: 0,
                                pointerEvents: 'none',
                            }}
                        />
                        <InteractiveGridPattern
                            width={40}
                            height={40}
                            squares={[24, 24]}
                            style={{
                                inset: 0,
                                top: '-2%',
                                height: '102%',
                                width: '100%',
                                transform: 'skewY(12deg)',
                                transformOrigin: 'center top',
                                zIndex: 1,
                                maskImage: 'radial-gradient(400px circle at center, white, transparent)',
                                WebkitMaskImage: 'radial-gradient(400px circle at center, white, transparent)',
                            }}
                        />
                        <div style={{ position: 'relative', zIndex: 2, marginTop: 'auto', paddingBottom: 8 }}>
                            <blockquote style={{ margin: 0 }}>
                                <p style={{ margin: 0, fontSize: 18, lineHeight: 1.7, fontWeight: 600 }}>
                                    &ldquo;This governance platform has unified our data operations and helped us deliver enterprise-grade intelligence to every team faster than ever before.&rdquo;
                                </p>
                            </blockquote>
                        </div>
                    </div>
                )}
                <div
                    style={{
                        ...authSurfaceStyle,
                        height: 'calc(100vh - 80px)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        padding: screens.md ? '32px 24px' : '16px',
                        overflowY: 'auto',
                    }}
                >
                    <Card
                        style={{
                            width: '100%',
                            maxWidth: 520,
                            borderRadius: 16,
                            border: '1px solid #ddddda',
                            boxShadow: '0 16px 40px rgba(15, 15, 15, .08)',
                            background: '#ffffff',
                        }}
                        styles={{ body: { padding: screens.md ? 24 : 18 } }}
                    >
                        <div style={{ marginBottom: 20, textAlign: 'center' }}>
                            <Typography.Title
                                level={3}
                                style={{
                                    margin: 0,
                                    color: '#141414',
                                    fontSize: 20,
                                    lineHeight: 1.15,
                                    fontWeight: 600,
                                    letterSpacing: '-0.02em',
                                }}
                            >
                                {displayTitle}
                            </Typography.Title>
                            <Typography.Paragraph
                                style={{
                                    marginTop: 8,
                                    marginBottom: 0,
                                    color: '#737373',
                                    fontSize: 14,
                                    lineHeight: 1.6,
                                }}
                            >
                                {displaySubtitle}
                            </Typography.Paragraph>
                        </div>
                        {checkingSession ? (
                            <div
                                style={{
                                    minHeight: 280,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                            >
                                <Spin size='large' />
                            </div>
                        ) : mode === 'login' ? renderLogin() : mode === 'register' ? renderRegister() : renderForgot()}
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
