import { request } from '@umijs/max';

type AccountCenterListParams = Record<string, any> & {
  count?: number;
};

export async function getAccountCenterCurrentUserDetail<T = any>(): Promise<T> {
  return request('/api/currentUserDetail');
}

export async function queryAccountCenterFakeListDetail<T = any>(
  params: AccountCenterListParams,
): Promise<T> {
  return request('/api/fake_list_Detail', {
    params,
  });
}

export async function getAccountSettingsCurrentUser<T = any>(): Promise<T> {
  return request('/api/accountSettingCurrentUser');
}

export async function queryGeographicProvince<T = any>(): Promise<T> {
  return request('/api/geographic/province');
}

export async function queryGeographicCity<T = any>(province: string): Promise<T> {
  return request(`/api/geographic/city/${province}`);
}
