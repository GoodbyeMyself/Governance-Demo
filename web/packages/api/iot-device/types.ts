export interface IotDeviceItem {
    id: number;
    deviceCode: string;
    deviceName: string;
    productKey?: string;
    productName?: string;
    deviceType: string;
    protocolType: string;
    endpoint?: string;
    connectionHost?: string;
    connectionPort?: number;
    topicOrPath?: string;
    username?: string;
    enabled?: boolean;
    onlineStatus?: string;
    lastOnlineAt?: string;
    lastOfflineAt?: string;
    status: string;
    description?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface IotDevicePayload {
    deviceCode: string;
    deviceName: string;
    productKey?: string;
    productName?: string;
    deviceType: string;
    protocolType: string;
    endpoint?: string;
    connectionHost?: string;
    connectionPort?: number;
    topicOrPath?: string;
    username?: string;
    passwordOrSecret?: string;
    enabled?: boolean;
    onlineStatus?: string;
    status: string;
    description?: string;
}
