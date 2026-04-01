import { request } from '@umijs/max';

export async function getFakeCaptcha(options?: { [key: string]: any }) {
  return request<API.BackendApiResponse<API.CaptchaResult>>('/api/auth-center/captcha', {
    method: 'GET',
    credentials: 'include',
    ...(options || {}),
  });
}
