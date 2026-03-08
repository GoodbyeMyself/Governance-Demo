import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
import proxy from './config/proxy';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '');
    const appEnv = env.REACT_APP_ENV || 'dev';
    const port = Number(env.PORT || 8001);

    return {
        plugins: [react()],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src'),
            },
        },
        css: {
            modules: {
                scopeBehaviour: 'local',
                globalModulePaths: [/global\.less$/],
            },
            preprocessorOptions: {
                less: {
                    javascriptEnabled: true,
                },
            },
        },
        server: {
            port,
            proxy: proxy[appEnv as keyof typeof proxy],
        },
        base: '/',
        build: {
            outDir: 'dist',
        },
    };
});
