import { defineConfig } from '@umijs/max';
import proxy from './proxy';

const { REACT_APP_ENV = 'dev' } = process.env;
const isProduction = process.env.NODE_ENV === 'production';

const CompressionPlugin = require('compression-webpack-plugin');
const productionGzipExtensions =
    /\.(js|css|json|txt|html|ico|svg|ttf|woff|woff2|eot|png|jpg|jpeg)(\?.*)?$/i;

export default defineConfig({
    chainWebpack(memo) {
        if (isProduction) {
            memo.plugin('compression-webpack-plugin').use(
                new CompressionPlugin({
                    algorithm: 'gzip',
                    test: productionGzipExtensions,
                    threshold: 10240,
                    minRatio: 0.8,
                }),
            );
        }
    },
    antd: {},
    access: {},
    model: {},
    initialState: {},
    request: {},
    layout: {
        title: '数据治理',
    },
    routes: [
        {
            path: '/',
            redirect: '/home',
        },
        {
            name: '系统工作台',
            path: '/home',
            component: './Home',
        },
        {
            name: '数据源管理',
            path: '/data-source',
            component: './DataSource',
        },
        {
            name: '元数据采集',
            path: '/metadata-collection',
            component: './MetadataCollection',
        },
        {
            path: '/metadata-collection/:id',
            component: './MetadataCollection/Detail',
            hideInMenu: true,
        },
        {
            name: '权限演示',
            path: '/access',
            component: './Access',
        },
    ],
    proxy: proxy[REACT_APP_ENV as keyof typeof proxy],
    npmClient: 'npm',
    qiankun: {
        slave: {},
    },
    favicons: ['/favicon.svg'],
    base: '/',
    publicPath: '/',
    outputPath: 'dist',
});
