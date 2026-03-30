import { request } from '@umijs/max';

export async function submitBasicForm<T = any>(params: any): Promise<T> {
  return request('/api/basicForm', {
    method: 'POST',
    data: params,
  });
}

export async function submitAdvancedForm<T = any>(params: any): Promise<T> {
  return request('/api/advancedForm', {
    method: 'POST',
    data: params,
  });
}

export async function submitStepForm<T = any>(params: any): Promise<T> {
  return request('/api/stepForm', {
    method: 'POST',
    data: params,
  });
}
