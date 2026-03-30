import { request } from '@umijs/max';

type ListParams = Record<string, any> & {
  count?: number;
};

export async function queryCommonList<T = any>(params: ListParams): Promise<T> {
  return request('/api/get_list', {
    params,
  });
}

export async function removeCommonList<T = any>(params: ListParams): Promise<T> {
  return request('/api/post_fake_list', {
    method: 'POST',
    data: {
      ...params,
      method: 'delete',
    },
  });
}

export async function addCommonList<T = any>(params: ListParams): Promise<T> {
  return request('/api/post_fake_list', {
    method: 'POST',
    data: {
      ...params,
      method: 'post',
    },
  });
}

export async function updateCommonList<T = any>(params: ListParams): Promise<T> {
  return request('/api/post_fake_list', {
    method: 'POST',
    data: {
      ...params,
      method: 'update',
    },
  });
}

export async function querySearchList<T = any>(params: ListParams): Promise<T> {
  return request('/api/fake_list', {
    params,
  });
}
