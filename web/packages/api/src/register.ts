import { request } from '@umijs/max';

export interface RegisterState {
  status?: 'ok' | 'error';
  currentAuthority?: 'user' | 'guest' | 'admin';
}

export interface UserRegisterParams {
  email: string;
  password: string;
  confirm: string;
  mobile: string;
  captcha: string;
  prefix: string;
}

export async function registerUser<T = any>(params: UserRegisterParams): Promise<T> {
  return request('/api/register', {
    method: 'POST',
    data: params,
  });
}
