import { request } from '@umijs/max';

export async function getAnalysisChartData<T = any>(): Promise<T> {
  return request('/api/fake_analysis_chart_data');
}

export async function queryProjectNotice<T = any>(): Promise<T> {
  return request('/api/project/notice');
}

export async function queryActivities<T = any>(): Promise<T> {
  return request('/api/activities');
}

export async function getWorkplaceChartData<T = any>(): Promise<T> {
  return request('/api/fake_workplace_chart_data');
}
