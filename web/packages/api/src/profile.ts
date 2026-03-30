import { request } from '@umijs/max';

export async function queryBasicProfile<T = any>(): Promise<T> {
  return request('/api/profile/basic');
}

export async function queryAdvancedProfile<T = any>(): Promise<T> {
  return request('/api/profile/advanced');
}
