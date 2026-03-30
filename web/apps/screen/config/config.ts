import { join } from 'node:path';
import { defineConfig } from '@umijs/max';

import routes from './routes';

const PUBLIC_PATH = '/';

export default defineConfig({
  alias: {
    '@api': join(__dirname, '../../../packages/api/src'),
    '@utils': join(__dirname, '../../../packages/utils/src'),
    '@i18n': join(__dirname, '../../../packages/i18n/src'),
    '@components': join(__dirname, '../../../packages/components/src'),
  } as Record<string, string>,

  hash: true,
  publicPath: PUBLIC_PATH,
  routes,

  locale: {
    default: 'zh-CN',
    antd: true,
    baseNavigator: true,
  },

  antd: {
    appConfig: {},
  },

  request: {},

  mako: {},
});
