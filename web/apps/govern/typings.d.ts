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

declare module '*.less' {
    const classes: Record<string, string>;
    export default classes;
}

export {};
