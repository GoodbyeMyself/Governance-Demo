import '@umijs/max/typings';

declare global {
    interface Window {
        __DATA_GOVERNANCE_CONFIG__?: {
            assistant?: {
                title?: string;
                iframeUrl?: string;
            };
        };
    }
}

export {};
