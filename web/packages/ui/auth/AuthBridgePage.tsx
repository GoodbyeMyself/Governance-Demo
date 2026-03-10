import {
    buildBridgeResponseMessage,
    clearCurrentAuthBridgePayload,
    getAllowedAuthBridgeOrigins,
    isBridgeRequestMessage,
    readCurrentAuthBridgePayload,
} from '@governance/utils';
import { useEffect } from 'react';

export const AuthBridgePage: React.FC = () => {
    useEffect(() => {
        const allowedOrigins = new Set(getAllowedAuthBridgeOrigins());

        const handleMessage = (event: MessageEvent<unknown>) => {
            if (!allowedOrigins.has(event.origin)) {
                return;
            }

            if (!isBridgeRequestMessage(event.data)) {
                return;
            }

            const source = event.source as WindowProxy | null;
            if (!source) {
                return;
            }

            if (event.data.type === 'REQUEST_AUTH_STATE') {
                const payload = readCurrentAuthBridgePayload();
                source.postMessage(
                    buildBridgeResponseMessage(
                        event.data.requestId,
                        payload ? 'AUTH_STATE' : 'AUTH_UNAVAILABLE',
                        payload || undefined,
                    ),
                    event.origin,
                );
                return;
            }

            clearCurrentAuthBridgePayload();
            source.postMessage(
                buildBridgeResponseMessage(event.data.requestId, 'AUTH_CLEARED'),
                event.origin,
            );
        };

        window.addEventListener('message', handleMessage);
        return () => window.removeEventListener('message', handleMessage);
    }, []);

    return null;
};
