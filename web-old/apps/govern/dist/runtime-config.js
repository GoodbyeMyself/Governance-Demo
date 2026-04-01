/* eslint-disable no-param-reassign */
/*
 * 数据治理平台运行时配置。
 * 构建后可直接修改 dist/runtime-config.js，无需重新打包前端。
 */
(function applyRuntimeConfig(windowObject) {
    const config = windowObject.__DATA_GOVERNANCE_CONFIG__ || {};
    windowObject.__DATA_GOVERNANCE_CONFIG__ = config;

    config.assistant = Object.assign(
        {
            title: '数据治理助手',
            iframeUrl: 'http://localhost:5173/chat?session=agent%3Amain%3Amain',
        },
        config.assistant || {},
    );

    config.apps = Object.assign(
        {
            governBaseUrl: 'http://127.0.0.1:19001',
            portalBaseUrl: 'http://127.0.0.1:19002',
        },
        config.apps || {},
    );
})(window);
