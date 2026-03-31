import { LoginPage } from '@components/user';
import { login } from '@/services/ant-design-pro/api';
import { getFakeCaptcha } from '@/services/ant-design-pro/login';
import Settings from '../../../../config/defaultSettings';

export default function Login() {
  return (
    <LoginPage
      productName="Govern"
      dashboardName="Govern Dashboard"
      titleSuffix={Settings.title}
      loginAction={login}
      getCaptchaAction={getFakeCaptcha}
    />
  );
}
