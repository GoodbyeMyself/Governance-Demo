import { join } from 'node:path';
import { defineConfig } from '@umijs/max';
import defaultSettings from './defaultSettings';
import proxy from './proxy';
import routes from './routes';

const { REACT_APP_ENV = 'dev' } = process.env;
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
  ignoreMomentLocale: true,
  proxy: proxy[REACT_APP_ENV as keyof typeof proxy],
  fastRefresh: true,
  model: {},
  initialState: {},
  title: 'Portal',
  layout: {
    locale: true,
    ...defaultSettings,
  },
  moment2dayjs: {
    preset: 'antd',
    plugins: ['duration'],
  },
  locale: {
    default: 'zh-CN',
    antd: true,
    baseNavigator: true,
  },
  antd: {
    appConfig: {},
    configProvider: {
      theme: {
        cssVar: true,
        token: {
          colorPrimary: defaultSettings.colorPrimary,
          fontFamily: 'AlibabaSans, sans-serif',
        },
      },
    },
  },
  request: {},
  access: {},
  headScripts: [
    { src: join(PUBLIC_PATH, 'scripts/loading.js'), async: true },
    { src: join(PUBLIC_PATH, 'iconfont/iconfont.js'), async: true },
  ],
  mock: {
    include: ['mock/**/*', 'src/pages/**/_mock.ts'],
  },
  mako: {},
  esbuildMinifyIIFE: true,
  exportStatic: {},
  define: {
    'process.env.CI': process.env.CI,
  },
});
