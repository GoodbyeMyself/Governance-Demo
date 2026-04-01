import {
  AlipayCircleOutlined,
  LockOutlined,
  TaobaoCircleOutlined,
  UserOutlined,
  WeiboCircleOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  ProFormCheckbox,
  ProFormText,
} from '@ant-design/pro-components';
import {
  FormattedMessage,
  Helmet,
  SelectLang,
  useIntl,
  useModel,
} from '@umijs/max';
import { Alert, App, Form, Grid, Input, Space } from 'antd';
import { createStyles } from 'antd-style';
import React, { useEffect, useState } from 'react';
import { flushSync } from 'react-dom';

export interface SharedLoginPageProps {
  dashboardName: string;
  getCaptchaAction: () => Promise<API.BackendApiResponse<API.CaptchaResult>>;
  loginAction: (params: API.LoginParams) => Promise<API.LoginResult>;
  productName: string;
  titleSuffix?: string;
}

const LOGIN_TEXT_FONT_FAMILY =
  "'PingFang SC', 'Microsoft YaHei', 'Noto Sans SC', 'Hiragino Sans GB', 'Source Han Sans SC', sans-serif";

const useStyles = createStyles(({ token }) => {
  return {
    action: {
      marginLeft: '8px',
      color: token.colorTextTertiary,
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      position: 'absolute',
      top: 12,
      right: 16,
      zIndex: 3,
      padding: '0px',
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      minHeight: '100vh',
      background: token.colorBgLayout,
      position: 'relative',
    },
    authCard: {
      position: 'relative',
      overflow: 'hidden',
      width: '100%',
      maxWidth: 480,
      borderRadius: 16,
      border: `1px solid ${token.colorPrimaryBorder}`,
      boxShadow: token.boxShadowSecondary,
      background: token.colorBgContainer,
      padding: '20px 20px 16px',
      '&::before': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: 3,
        background: `linear-gradient(90deg, ${token.colorPrimary}, ${token.colorPrimaryHover})`,
      },
    },
    authCardMobile: {
      padding: '16px 16px 14px',
    },
    formTypography: {
      fontFamily: LOGIN_TEXT_FONT_FAMILY,
      '& .ant-input, & .ant-input-affix-wrapper, & .ant-input-password, & .ant-alert-message, & .ant-checkbox-wrapper, & .ant-btn, & .ant-form-item-explain-error, & .ant-pro-form-login-page-desc, & .ant-pro-form-login-page-title, & .ant-pro-form-login-page-main': {
        fontFamily: LOGIN_TEXT_FONT_FAMILY,
      },
      '& input::placeholder': {
        color: token.colorTextPlaceholder,
        opacity: 1,
        fontWeight: 400,
      },
      '& .ant-input-affix-wrapper > input.ant-input::placeholder': {
        color: token.colorTextPlaceholder,
        opacity: 1,
      },
    },
    forgotLink: {
      float: 'right',
      color: token.colorPrimary,
      transition: 'color 0.2s ease',
      '&:hover': {
        color: token.colorPrimaryHover,
      },
    },
    captchaSection: {
      marginBottom: 24,
    },
    captchaFormItem: {
      marginBottom: 0,
      '& .ant-input-affix-wrapper': {
        position: 'relative',
        borderStartEndRadius: 0,
        borderEndEndRadius: 0,
        borderInlineEnd: 0,
        boxShadow: 'none',
        zIndex: 1,
        '&:hover, &:focus, &.ant-input-affix-wrapper-focused': {
          zIndex: 2,
          borderInlineEnd: 0,
          boxShadow: 'none',
        },
      },
      '& .ant-form-item-explain-error': {
        marginTop: 8,
        fontFamily: LOGIN_TEXT_FONT_FAMILY,
      },
    },
    captchaCompact: {
      display: 'flex',
      width: '100%',
      borderRadius: token.borderRadius,
      transition: 'box-shadow 0.2s ease',
      '&:focus-within': {
        boxShadow: `0 0 0 2px ${token.colorPrimaryBg}`,
      },
      '&:focus-within .ant-input-affix-wrapper': {
        borderColor: token.colorPrimary,
      },
      '&:focus-within .captcha-trigger': {
        borderColor: token.colorPrimary,
      },
    },
    captchaTrigger: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: 120,
      height: 32,
      padding: 0,
      border: `1px solid ${token.colorBorder}`,
      borderInlineStart: 0,
      borderStartEndRadius: token.borderRadius,
      borderEndEndRadius: token.borderRadius,
      background: token.colorBgContainer,
      cursor: 'pointer',
      overflow: 'hidden',
      transition: 'background-color 0.2s ease, opacity 0.2s ease',
      '&:hover': {
        background: token.colorFillAlter,
      },
      '&:disabled': {
        cursor: 'wait',
        opacity: 0.72,
      },
      '&:focus-visible': {
        outline: 'none',
      },
    },
    captchaImage: {
      display: 'block',
      width: 120,
      height: 32,
      objectFit: 'cover',
      backgroundColor: token.colorBgLayout,
    },
    captchaHint: {
      marginTop: 8,
      color: token.colorTextDescription,
      fontFamily: LOGIN_TEXT_FONT_FAMILY,
      fontSize: token.fontSizeSM,
      fontWeight: 400,
      lineHeight: 1.6,
    },
  };
});

const LOGO_ICON = (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ width: 24, height: 24, marginRight: 8, flexShrink: 0 }}
  >
    <path d="M15 6v12a3 3 0 1 0 3-3H6a3 3 0 1 0 3 3V6a3 3 0 1 0-3 3h12a3 3 0 1 0-3-3" />
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

const ActionIcons = () => {
  const { styles } = useStyles();

  return (
    <>
      <AlipayCircleOutlined
        key="AlipayCircleOutlined"
        className={styles.action}
      />
      <TaobaoCircleOutlined
        key="TaobaoCircleOutlined"
        className={styles.action}
      />
      <WeiboCircleOutlined
        key="WeiboCircleOutlined"
        className={styles.action}
      />
    </>
  );
};

const Lang = () => {
  const { styles } = useStyles();

  return (
    <div className={styles.lang} data-lang>
      {SelectLang && <SelectLang />}
    </div>
  );
};

const LoginMessage: React.FC<{
  content: string;
}> = ({ content }) => {
  return (
    <Alert
      style={{
        marginBottom: 24,
      }}
      message={content}
      type="error"
      showIcon
    />
  );
};

function getRequestErrorMessage(error: unknown): string | undefined {
  const responseError = error as {
    response?: {
      data?: {
        message?: string;
      };
    };
  };

  return responseError.response?.data?.message;
}

export default function LoginPage({
  dashboardName,
  getCaptchaAction,
  loginAction,
  productName,
  titleSuffix,
}: SharedLoginPageProps) {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [captcha, setCaptcha] = useState<API.CaptchaResult>();
  const [captchaLoading, setCaptchaLoading] = useState(false);
  const { initialState, setInitialState } = useModel('@@initialState');
  const { styles, cx } = useStyles();
  const { message } = App.useApp();
  const intl = useIntl();
  const screens = Grid.useBreakpoint();

  const fetchUserInfo = async () => {
    const userInfo = await initialState?.fetchUserInfo?.();
    if (userInfo) {
      flushSync(() => {
        setInitialState((s: any) => ({
          ...s,
          currentUser: userInfo,
        }));
      });
    }
  };

  const loadCaptcha = async () => {
    setCaptchaLoading(true);

    try {
      const result = await getCaptchaAction();
      if (result.success) {
        setCaptcha(result.data);
      } else {
        message.error(result.message || '验证码获取失败');
      }
    } catch {
      message.error('验证码获取失败，请重试');
    } finally {
      setCaptchaLoading(false);
    }
  };

  useEffect(() => {
    void loadCaptcha();
  }, []);

  const handleSubmit = async (values: API.LoginParams) => {
    if (!captcha?.captchaId) {
      await loadCaptcha();
      message.warning('验证码已刷新，请重新输入');
      return;
    }

    try {
      const result = await loginAction({
        ...values,
        type: 'account',
        captchaId: captcha.captchaId,
      });

      if (result.success) {
        const defaultLoginSuccessMessage = intl.formatMessage({
          id: 'pages.login.success',
          defaultMessage: '登录成功',
        });
        message.success(defaultLoginSuccessMessage);
        setUserLoginState({});
        await fetchUserInfo();

        const urlParams = new URL(window.location.href).searchParams;
        window.location.href = urlParams.get('redirect') || '/';
        return;
      }

      setUserLoginState({
        status: 'error',
        type: 'account',
        message: result.message,
      });
      await loadCaptcha();
    } catch (error) {
      const errorMessage =
        getRequestErrorMessage(error) ||
        intl.formatMessage({
          id: 'pages.login.failure',
          defaultMessage: '登录失败，请重试',
        });

      setUserLoginState({
        status: 'error',
        type: 'account',
        message: errorMessage,
      });
      message.error(errorMessage);
      await loadCaptcha();
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {intl.formatMessage({
            id: 'menu.login',
            defaultMessage: '登录页',
          })}
          {titleSuffix && ` - ${titleSuffix}`}
        </title>
      </Helmet>
      <Lang />
      <div
        style={{
          height: '100vh',
          display: 'grid',
          gridTemplateColumns: screens.lg
            ? 'minmax(420px, 1fr) minmax(420px, 1fr)'
            : '1fr',
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
              height: 'calc(100vh - 0px)',
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
              {dashboardName}
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
                maskImage:
                  'radial-gradient(400px circle at center, white, transparent)',
                WebkitMaskImage:
                  'radial-gradient(400px circle at center, white, transparent)',
              }}
            />
            <div
              style={{
                position: 'relative',
                zIndex: 2,
                marginTop: 'auto',
                paddingBottom: 8,
              }}
            >
              <blockquote style={{ margin: 0 }}>
                <p
                  style={{
                    margin: 0,
                    fontSize: 18,
                    lineHeight: 1.7,
                    fontWeight: 600,
                  }}
                >
                  &ldquo;This governance platform has unified our data operations
                  and helped us deliver enterprise-grade intelligence to every
                  team faster than ever before.&rdquo;
                </p>
              </blockquote>
            </div>
          </div>
        )}
        <div
          style={{
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: screens.md ? '32px 24px' : '16px',
            overflowY: 'auto',
          }}
        >
          <div
            className={cx(styles.authCard, !screens.md && styles.authCardMobile)}
          >
            <LoginForm
              className={styles.formTypography}
              contentStyle={{
                minWidth: 280,
                maxWidth: '100%',
              }}
              logo={<img alt="logo" src="/logo.svg" />}
              title={productName}
              subTitle={intl.formatMessage({
                id: 'pages.layouts.userLayout.title',
              })}
              initialValues={{
                autoLogin: true,
              }}
              actions={[
                <FormattedMessage
                  key="loginWith"
                  id="pages.login.loginWith"
                  defaultMessage="其他登录方式"
                />,
                <ActionIcons key="icons" />,
              ]}
              onFinish={async (values: API.LoginParams) => {
                await handleSubmit(values);
              }}
            >
              {userLoginState.status === 'error' && (
                <LoginMessage
                  content={
                    userLoginState.message ||
                    intl.formatMessage({
                      id: 'pages.login.accountLogin.errorMessage',
                      defaultMessage: '账户、密码或验证码错误',
                    })
                  }
                />
              )}

              <ProFormText
                name="username"
                fieldProps={{
                  prefix: <UserOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.username.placeholder',
                  defaultMessage: '请输入用户名（admin）',
                })}
                rules={[
                  {
                    required: true,
                    message: (
                      <FormattedMessage
                        id="pages.login.username.required"
                        defaultMessage="请输入用户名"
                      />
                    ),
                  },
                ]}
              />

              <ProFormText.Password
                name="password"
                fieldProps={{
                  prefix: <LockOutlined />,
                }}
                placeholder={intl.formatMessage({
                  id: 'pages.login.password.placeholder',
                  defaultMessage: '请输入密码（Admin@123456）',
                })}
                rules={[
                  {
                    required: true,
                    message: (
                      <FormattedMessage
                        id="pages.login.password.required"
                        defaultMessage="请输入密码"
                      />
                    ),
                  },
                ]}
              />

              <div className={styles.captchaSection}>
                <Form.Item
                  className={styles.captchaFormItem}
                >
                  <Space.Compact block className={styles.captchaCompact}>
                    <Form.Item
                      name="captchaCode"
                      noStyle
                      rules={[
                        {
                          required: true,
                          message: '请输入图形验证码',
                        },
                      ]}
                    >
                      <Input
                        maxLength={12}
                        placeholder="请输入图形验证码"
                        prefix={<LockOutlined />}
                      />
                    </Form.Item>
                    <button
                      className={`${styles.captchaTrigger} captcha-trigger`}
                      disabled={captchaLoading}
                      onClick={() => {
                        void loadCaptcha();
                      }}
                      title={captchaLoading ? '验证码刷新中' : '点击刷新验证码'}
                      type="button"
                    >
                      {captcha?.imageData ? (
                        <img
                          alt="captcha"
                          className={styles.captchaImage}
                          src={captcha.imageData}
                        />
                      ) : (
                        <div className={styles.captchaImage} />
                      )}
                    </button>
                  </Space.Compact>
                </Form.Item>
                <div className={styles.captchaHint}>
                  验证码区分大小写，登录失败后会自动刷新。
                </div>
              </div>

              <div
                style={{
                  marginBottom: 24,
                }}
              >
                <ProFormCheckbox noStyle name="autoLogin">
                  <FormattedMessage
                    id="pages.login.rememberMe"
                    defaultMessage="自动登录"
                  />
                </ProFormCheckbox>
                <a className={styles.forgotLink}>
                  <FormattedMessage
                    id="pages.login.forgotPassword"
                    defaultMessage="忘记密码"
                  />
                </a>
              </div>
            </LoginForm>
          </div>
        </div>
      </div>
    </div>
  );
}
