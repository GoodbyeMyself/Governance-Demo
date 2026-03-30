import { request } from '@umijs/max';

export async function queryTagList<T = any>(): Promise<T> {
  return request('/api/tags');
}
