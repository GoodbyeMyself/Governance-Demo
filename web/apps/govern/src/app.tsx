import { LinkOutlined, SettingOutlined } from '@ant-design/icons';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import { SettingDrawer } from '@ant-design/pro-components';
import type { RequestConfig, RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import React, { useEffect, useState } from 'react';
import {
  AvatarDropdown,
  AvatarName,
  Footer,
  Question,
  SelectLang,
} from '@/components';
import { currentUser as queryCurrentUser } from '@/services/ant-design-pro/api';
import defaultSettings from '../config/defaultSettings';
import { errorConfig } from './requestErrorConfig';
import '@ant-design/v5-patch-for-react-19';

const isDev = process.env.NODE_ENV === 'development' || process.env.CI;
const loginPath = '/user/login';
const authFreePaths = [loginPath, '/user/register', '/user/register-result'];
const OPEN_GLOBAL_SETTING_EVENT = 'open-global-setting';

function redirectToLogin() {
  const { pathname, search } = history.location;

  if (authFreePaths.includes(pathname)) {
    return;
  }

  const redirect = pathname + search;
  history.replace({
    pathname: loginPath,
    search: redirect ? `?redirect=${encodeURIComponent(redirect)}` : undefined,
  });
}

const GlobalSettingDrawer = ({
  settings,
  onSettingChange,
}: {
  settings?: Partial<LayoutSettings>;
  onSettingChange: (settings: Partial<LayoutSettings>) => void;
}) => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const handleOpen = () => setOpen(true);

    window.addEventListener(OPEN_GLOBAL_SETTING_EVENT, handleOpen);

    return () => {
      window.removeEventListener(OPEN_GLOBAL_SETTING_EVENT, handleOpen);
    };
  }, []);

  return (
    <SettingDrawer
      collapse={open}
      disableUrlParams
      enableDarkTheme
      settings={settings}
      onCollapseChange={setOpen}
      onSettingChange={onSettingChange}
    />
  );
};

export async function getInitialState(): Promise<{
  settings?: Partial<LayoutSettings>;
  currentUser?: API.CurrentUser;
  loading?: boolean;
  fetchUserInfo?: () => Promise<API.CurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    try {
      const response = await queryCurrentUser({
        skipErrorHandler: true,
      });
      return response.data;
    } catch (error: any) {
      if (error?.response?.status === 401) {
        redirectToLogin();
        return undefined;
      }

      throw error;
    }
  };

  const { location } = history;
  const isAuthPage = authFreePaths.includes(location.pathname);

  if (!isAuthPage) {
    try {
      const currentUser = await fetchUserInfo();
      return {
        fetchUserInfo,
        currentUser,
        settings: defaultSettings as Partial<LayoutSettings>,
      };
    } catch {
      return {
        fetchUserInfo,
        settings: defaultSettings as Partial<LayoutSettings>,
      };
    }
  }

  return {
    fetchUserInfo,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

export const layout: RunTimeLayoutConfig = ({
  initialState,
  setInitialState,
}) => {
  return {
    actionsRender: () => [
      <Question
        key="theme"
        navTheme={
          (initialState?.settings?.navTheme as 'light' | 'realDark') ?? 'light'
        }
        onThemeChange={(navTheme) => {
          setInitialState((preInitialState: typeof initialState) => ({
            ...preInitialState,
            settings: {
              ...preInitialState?.settings,
              navTheme,
            },
          }));
        }}
      />,
      <SelectLang key="SelectLang" />,
    ],
    avatarProps: {
      src: initialState?.currentUser?.avatar,
      title: <AvatarName />,
      render: (_: unknown, avatarChildren: React.ReactNode) => {
        return <AvatarDropdown menu>{avatarChildren}</AvatarDropdown>;
      },
    },
    waterMarkProps: {
      content: initialState?.currentUser?.name,
    },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { location } = history;
      const isAuthPage = authFreePaths.includes(location.pathname);

      if (!initialState?.currentUser && !isAuthPage) {
        redirectToLogin();
      }
    },
    bgLayoutImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr',
        bottom: -68,
        right: -45,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr',
        bottom: 0,
        left: 0,
        width: '331px',
      },
    ],
    links: [
      <a
        key="global-setting"
        href="#"
        onClick={(event: React.MouseEvent<HTMLAnchorElement>) => {
          event.preventDefault();
          window.dispatchEvent(new Event(OPEN_GLOBAL_SETTING_EVENT));
        }}
      >
        <SettingOutlined />
        <span>全局配置</span>
      </a>,
      <a
        key="help-center"
        href="https://pro.ant.design/zh-CN/docs/getting-started"
        target="_blank"
        rel="noreferrer"
      >
        <LinkOutlined />
        <span>帮助中心</span>
      </a>,
    ],
    menuHeaderRender: undefined,
    childrenRender: (children) => {
      return (
        <>
          {children}
          {isDev && (
            <GlobalSettingDrawer
              settings={initialState?.settings}
              onSettingChange={(settings) => {
                setInitialState((preInitialState: typeof initialState) => ({
                  ...preInitialState,
                  settings,
                }));
              }}
            />
          )}
        </>
      );
    },
    ...initialState?.settings,
  };
};

export const request: RequestConfig = {
  ...errorConfig,
};
