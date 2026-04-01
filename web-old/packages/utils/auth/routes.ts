export const LOGIN_PATH = '/login';
export const AUTH_BRIDGE_PATH = '/auth-bridge';
export const HOME_PATH = '/home';
export const PORTAL_DEMO_PATH = '/demo';
export const DATA_SOURCE_PATH = '/data-source';
export const IOT_DEVICE_PATH = '/iot-device';
export const IOT_COLLECTION_PATH = '/iot-collection';
export const METADATA_COLLECTION_PATH = '/metadata-collection';
export const USER_MANAGEMENT_PATH = '/user-management';
export const ROLE_MANAGEMENT_PATH = '/role-management';
export const PROFILE_PATH = '/profile';
export const ACCESS_PATH = '/access';

export const buildIotDeviceDetailPath = (id: number | string) =>
    `${IOT_DEVICE_PATH}/${id}`;

export const buildIotCollectionDetailPath = (id: number | string) =>
    `${IOT_COLLECTION_PATH}/${id}`;

export const buildMetadataCollectionDetailPath = (id: number | string) =>
    `${METADATA_COLLECTION_PATH}/${id}`;

export const buildLoginRedirect = (pathname: string, search = '') => {
    const redirect = encodeURIComponent(`${pathname}${search}`);
    return `${LOGIN_PATH}?redirect=${redirect}`;
};

export type AppName = 'govern' | 'portal';

type RuntimeAppConfig = {
    apps?: {
        governBaseUrl?: string;
        portalBaseUrl?: string;
    };
};

const APP_PORTS: Record<AppName, string[]> = {
    govern: ['8001', '19001'],
    portal: ['8002', '19002'],
};

const SIBLING_PORTS: Record<AppName, Record<string, string>> = {
    govern: {
        '8002': '8001',
        '19002': '19001',
    },
    portal: {
        '8001': '8002',
        '19001': '19002',
    },
};

const hasWindow = () => typeof window !== 'undefined';

export const getSiblingAppName = (appName: AppName): AppName =>
    appName === 'govern' ? 'portal' : 'govern';

const getRuntimeAppConfig = (): RuntimeAppConfig | undefined => {
    if (!hasWindow()) {
        return undefined;
    }

    return (
        window as Window & {
            __DATA_GOVERNANCE_CONFIG__?: RuntimeAppConfig;
        }
    ).__DATA_GOVERNANCE_CONFIG__;
};

const normalizeBaseUrl = (baseUrl?: string) => {
    if (!baseUrl || !hasWindow()) {
        return undefined;
    }

    try {
        return new URL(baseUrl, window.location.origin).toString();
    } catch {
        return undefined;
    }
};

const deriveAppBaseUrl = (appName: AppName) => {
    if (!hasWindow()) {
        return '';
    }

    const currentUrl = new URL(window.location.origin);
    if (APP_PORTS[appName].includes(currentUrl.port)) {
        return currentUrl.origin;
    }

    const siblingPort = SIBLING_PORTS[appName][currentUrl.port];
    if (siblingPort) {
        currentUrl.port = siblingPort;
        return currentUrl.origin;
    }

    return currentUrl.origin;
};

const getAppBaseUrl = (appName: AppName) => {
    const runtimeConfig = getRuntimeAppConfig()?.apps;
    const configuredBaseUrl =
        appName === 'govern'
            ? runtimeConfig?.governBaseUrl
            : runtimeConfig?.portalBaseUrl;

    return normalizeBaseUrl(configuredBaseUrl) || deriveAppBaseUrl(appName);
};

const buildAppUrl = (appName: AppName, pathname = HOME_PATH) => {
    const baseUrl = getAppBaseUrl(appName);
    if (!baseUrl) {
        return pathname;
    }

    const base = new URL(baseUrl);
    const normalizedPath = pathname.replace(/^\//, '');
    return new URL(
        normalizedPath,
        `${base.toString().replace(/\/$/, '')}/`,
    ).toString();
};

export const buildGovernAppUrl = (pathname = HOME_PATH) =>
    buildAppUrl('govern', pathname);

export const buildPortalAppUrl = (pathname = HOME_PATH) =>
    buildAppUrl('portal', pathname);

export const getAppOrigin = (appName: AppName) => {
    const baseUrl = getAppBaseUrl(appName);
    if (!baseUrl) {
        return '';
    }

    try {
        return new URL(baseUrl).origin;
    } catch {
        return '';
    }
};
