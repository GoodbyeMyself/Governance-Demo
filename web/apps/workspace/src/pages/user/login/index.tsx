import React from 'react';
import { LoginPage } from '@components/user';
import { login } from '@/services/ant-design-pro/api';
import { getFakeCaptcha } from '@/services/ant-design-pro/login';
import Settings from '../../../../config/defaultSettings';

export default function Login() {
  const titleSuffix =
    typeof Settings.title === 'string' ? Settings.title : undefined;

  return (
    <LoginPage
      productName="Govern"
      dashboardName="Govern Dashboard"
      titleSuffix={titleSuffix}
      loginAction={login}
      getCaptchaAction={getFakeCaptcha}
    />
  );
}
