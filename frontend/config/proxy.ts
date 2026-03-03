export default {
    dev: {
        '/api': {
            target: 'http://localhost:8080',
            changeOrigin: true,
        },
        '/service-bms/': {
            target: 'https://m1.apifoxmock.com/m1/4279578-3921636-default/',
            changeOrigin: true,
            pathRewrite: {
                '^': '',
            },
        },
    },
};
