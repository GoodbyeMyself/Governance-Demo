import { UserOutlined } from '@ant-design/icons';
import { fetchCurrentUser, logout as logoutRequest } from '@/services/authCenter';
import {
    clearAuthState,
    getStoredUser,
    getToken,
    hasToken,
    setAuthState,
} from '@/utils/auth';
import { history, type RunTimeLayoutConfig } from '@umijs/max';
import { Avatar, Dropdown, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import React from 'react';

type AppProps = {};

const LOGIN_PATH = '/login';
const PROFILE_PATH = '/profile';

const redirectToLogin = () => {
    const pathname = history.location?.pathname || '/';
    if (pathname === LOGIN_PATH) {
        return;
    }

    const search = history.location?.search || '';
    const redirect = encodeURIComponent(`${pathname}${search}`);
    history.push(`${LOGIN_PATH}?redirect=${redirect}`);
};

const handleLogout = async () => {
    try {
        await logoutRequest();
    } catch {
        // 退出接口失败时，不阻塞本地清理。
    } finally {
        clearAuthState();
        history.push(LOGIN_PATH);
    }
};

export const qiankun = {
    async bootstrap(props: AppProps) {
        console.log(props, '微应用：数据治理 - 加载前');
    },
    async mount(props: AppProps) {
        console.log(props, '微应用：数据治理 - 挂载成功');
    },
    async unmount(props: AppProps) {
        console.log(props, '微应用：数据治理 - 卸载成功');
    },
};

export async function getInitialState(): Promise<{
    isMicroApp?: boolean;
    currentUser?: ReturnType<typeof getStoredUser>;
}> {
    const isMicroApp = (window as any).__POWERED_BY_QIANKUN__ ? true : false;
    const localUser = getStoredUser();

    if (!hasToken()) {
        return {
            isMicroApp,
            currentUser: localUser,
        };
    }

    const token = getToken();
    if (!token) {
        return {
            isMicroApp,
            currentUser: localUser,
        };
    }

    try {
        const res = await fetchCurrentUser();
        if (res.success && res.data) {
            setAuthState(token, res.data);
            return {
                isMicroApp,
                currentUser: res.data,
            };
        }
        clearAuthState();
        redirectToLogin();
        return {
            isMicroApp,
            currentUser: null,
        };
    } catch {
        clearAuthState();
        redirectToLogin();
        return {
            isMicroApp,
            currentUser: null,
        };
    }
}

export const layout: RunTimeLayoutConfig = ({ initialState }) => {
    const { isMicroApp } = initialState || {};

    return {
        pure: isMicroApp,
        logo: 'https://img.alicdn.com/tfs/TB1YHEpwUT1gK0jSZFhXXaAtVXa-28-27.svg',
        menu: {
            locale: false,
        },
        layout: 'top',
        pageTitleRender: () => '数据治理平台',
        onPageChange: () => {
            const pathname = history.location?.pathname || '/';

            if (pathname === LOGIN_PATH) {
                if (hasToken()) {
                    const search = new URLSearchParams(history.location?.search || '');
                    const redirect = search.get('redirect');
                    history.replace(redirect || '/home');
                }
                return;
            }

            if (!hasToken()) {
                redirectToLogin();
            }
        },
        actionsRender: () => {
            if (!hasToken()) {
                return [];
            }

            const currentUser = getStoredUser();
            const displayName =
                currentUser?.nickname?.trim() || currentUser?.username || '当前用户';

            const menuProps: MenuProps = {
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
                onClick: ({ key }) => {
                    if (key === 'profile') {
                        history.push(PROFILE_PATH);
                        return;
                    }
                    if (key === 'logout') {
                        void handleLogout();
                    }
                },
            };

            return [
                React.createElement(
                    Dropdown,
                    {
                        key: 'user-dropdown',
                        menu: menuProps,
                        trigger: ['hover'],
                        placement: 'bottomRight',
                    },
                    React.createElement(
                        Space,
                        {
                            size: 8,
                            align: 'center',
                            style: {
                                cursor: 'pointer',
                                height: 32,
                                lineHeight: '32px',
                                padding: '0 2px',
                                marginRight: 24,
                            },
                        },
                        React.createElement(Avatar, {
                            size: 26,
                            icon: React.createElement(UserOutlined),
                        }),
                        React.createElement(
                            Typography.Text,
                            { style: { lineHeight: '22px' } },
                            displayName,
                        ),
                    ),
                ),
            ];
        },
    };
};
