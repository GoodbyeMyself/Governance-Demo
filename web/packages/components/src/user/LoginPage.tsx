import {
  AlipayCircleOutlined,
  LockOutlined,
  MobileOutlined,
  TaobaoCircleOutlined,
  UserOutlined,
  WeiboCircleOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  ProFormCaptcha,
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
import { Alert, App, Grid, Tabs } from 'antd';
import { createStyles } from 'antd-style';
import React, { useState } from 'react';
import { flushSync } from 'react-dom';

export interface SharedLoginPageProps {
  dashboardName: string;
  getCaptchaAction: (params: { phone: string }) => Promise<unknown>;
  loginAction: (params: API.LoginParams) => Promise<API.LoginResult>;
  productName: string;
  titleSuffix?: string;
}

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
      maxWidth: 520,
      borderRadius: 16,
      border: `1px solid ${token.colorPrimaryBorder}`,
      boxShadow: token.boxShadowSecondary,
      background: token.colorBgContainer,
      padding: '24px 24px 20px',
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
      padding: '18px 18px 16px',
    },
    authTabs: {
      marginBottom: 24,
      '& .ant-tabs-nav': {
        marginBottom: 24,
      },
      '& .ant-tabs-tab': {
        color: token.colorTextSecondary,
        transition: 'color 0.2s ease',
      },
      '& .ant-tabs-tab:hover': {
        color: token.colorPrimaryHover,
      },
      '& .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn': {
        color: token.colorPrimary,
        fontWeight: 600,
      },
      '& .ant-tabs-ink-bar': {
        background: token.colorPrimary,
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

export default function LoginPage({
  dashboardName,
  getCaptchaAction,
  loginAction,
  productName,
  titleSuffix,
}: SharedLoginPageProps) {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
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

  const handleSubmit = async (values: API.LoginParams) => {
    try {
      const msg = await loginAction({ ...values, type });
      if (msg.status === 'ok') {
        const defaultLoginSuccessMessage = intl.formatMessage({
          id: 'pages.login.success',
          defaultMessage: '登录成功！',
        });
        message.success(defaultLoginSuccessMessage);
        await fetchUserInfo();
        const urlParams = new URL(window.location.href).searchParams;
        window.location.href = urlParams.get('redirect') || '/';
        return;
      }
      setUserLoginState(msg);
    } catch (error) {
      const defaultLoginFailureMessage = intl.formatMessage({
        id: 'pages.login.failure',
        defaultMessage: '登录失败，请重试！',
      });
      console.log(error);
      message.error(defaultLoginFailureMessage);
    }
  };
  const { status, type: loginType } = userLoginState;

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
              onFinish={async (values: any) => {
                await handleSubmit(values as API.LoginParams);
              }}
            >
              <Tabs
                className={styles.authTabs}
                activeKey={type}
                onChange={setType}
                centered
                items={[
                  {
                    key: 'account',
                    label: intl.formatMessage({
                      id: 'pages.login.accountLogin.tab',
                      defaultMessage: '账户密码登录',
                    }),
                  },
                  {
                    key: 'mobile',
                    label: intl.formatMessage({
                      id: 'pages.login.phoneLogin.tab',
                      defaultMessage: '手机号登录',
                    }),
                  },
                ]}
              />

              {status === 'error' && loginType === 'account' && (
                <LoginMessage
                  content={intl.formatMessage({
                    id: 'pages.login.accountLogin.errorMessage',
                    defaultMessage: '账户或密码错误(admin/ant.design)',
                  })}
                />
              )}
              {type === 'account' && (
                <>
                  <ProFormText
                    name="username"
                    fieldProps={{
                      size: 'large',
                      prefix: <UserOutlined />,
                    }}
                    placeholder={intl.formatMessage({
                      id: 'pages.login.username.placeholder',
                      defaultMessage: '用户名: admin or user',
                    })}
                    rules={[
                      {
                        required: true,
                        message: (
                          <FormattedMessage
                            id="pages.login.username.required"
                            defaultMessage="请输入用户名!"
                          />
                        ),
                      },
                    ]}
                  />
                  <ProFormText.Password
                    name="password"
                    fieldProps={{
                      size: 'large',
                      prefix: <LockOutlined />,
                    }}
                    placeholder={intl.formatMessage({
                      id: 'pages.login.password.placeholder',
                      defaultMessage: '密码: ant.design',
                    })}
                    rules={[
                      {
                        required: true,
                        message: (
                          <FormattedMessage
                            id="pages.login.password.required"
                            defaultMessage="请输入密码！"
                          />
                        ),
                      },
                    ]}
                  />
                </>
              )}

              {status === 'error' && loginType === 'mobile' && (
                <LoginMessage content="验证码错误" />
              )}
              {type === 'mobile' && (
                <>
                  <ProFormText
                    fieldProps={{
                      size: 'large',
                      prefix: <MobileOutlined />,
                    }}
                    name="mobile"
                    placeholder={intl.formatMessage({
                      id: 'pages.login.phoneNumber.placeholder',
                      defaultMessage: '手机号',
                    })}
                    rules={[
                      {
                        required: true,
                        message: (
                          <FormattedMessage
                            id="pages.login.phoneNumber.required"
                            defaultMessage="请输入手机号？"
                          />
                        ),
                      },
                      {
                        pattern: /^1\d{10}$/,
                        message: (
                          <FormattedMessage
                            id="pages.login.phoneNumber.invalid"
                            defaultMessage="手机号格式错误！"
                          />
                        ),
                      },
                    ]}
                  />
                  <ProFormCaptcha
                    fieldProps={{
                      size: 'large',
                      prefix: <LockOutlined />,
                    }}
                    captchaProps={{
                      size: 'large',
                    }}
                    placeholder={intl.formatMessage({
                      id: 'pages.login.captcha.placeholder',
                      defaultMessage: '请输入验证码',
                    })}
                    captchaTextRender={(timing, count) => {
                      if (timing) {
                        return `${count} ${intl.formatMessage({
                          id: 'pages.getCaptchaSecondText',
                          defaultMessage: '获取验证码',
                        })}`;
                      }
                      return intl.formatMessage({
                        id: 'pages.login.phoneLogin.getVerificationCode',
                        defaultMessage: '获取验证码',
                      });
                    }}
                    name="captcha"
                    rules={[
                      {
                        required: true,
                        message: (
                          <FormattedMessage
                            id="pages.login.captcha.required"
                            defaultMessage="请输入验证码？"
                          />
                        ),
                      },
                    ]}
                    onGetCaptcha={async (phone) => {
                      const result = await getCaptchaAction({
                        phone,
                      });
                      if (!result) {
                        return;
                      }
                      message.success('获取验证码成功！验证码为：1234');
                    }}
                  />
                </>
              )}
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
