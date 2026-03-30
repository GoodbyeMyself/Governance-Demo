import { request } from '@umijs/max';

type CardListParams = Record<string, any> & {
  count?: number;
};

export async function queryCardFakeList<T = any>(params: CardListParams): Promise<T> {
  return request('/api/card_fake_list', {
    params,
  });
}
