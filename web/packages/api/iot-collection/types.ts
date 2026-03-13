export interface IotCollectionTaskItem {
    id: number;
    taskName: string;
    deviceId: number;
    deviceCode: string;
    deviceName: string;
    productKey?: string;
    productName?: string;
    collectionType: string;
    scheduleType: string;
    cronExpression?: string;
    pollIntervalSeconds?: number;
    sourceType?: string;
    dataFormat?: string;
    configJson?: string;
    enabled: boolean;
    description?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface IotCollectionTaskPayload {
    taskName: string;
    deviceId: number;
    productKey?: string;
    productName?: string;
    collectionType: string;
    scheduleType: string;
    cronExpression?: string;
    pollIntervalSeconds?: number;
    sourceType?: string;
    dataFormat?: string;
    configJson?: string;
    enabled: boolean;
    description?: string;
}

export interface IotTelemetryLatestItem {
    id: number;
    deviceId: number;
    deviceCode: string;
    deviceName: string;
    productKey?: string;
    productName?: string;
    metricCode: string;
    metricName: string;
    metricValue: string;
    valueType?: string;
    unit?: string;
    quality?: string;
    collectedAt?: string;
    receivedAt?: string;
    sourceType?: string;
    dataFormat?: string;
    payloadJson?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface IotTelemetryHistoryItem {
    id: number;
    deviceId: number;
    taskId?: number;
    deviceCode: string;
    deviceName: string;
    productKey?: string;
    productName?: string;
    metricCode: string;
    metricName: string;
    metricValue: string;
    valueType?: string;
    unit?: string;
    quality?: string;
    collectedAt?: string;
    receivedAt?: string;
    sourceType?: string;
    dataFormat?: string;
    payloadJson?: string;
    createdAt?: string;
}

export interface IotTelemetryOverview {
    totalTelemetryDevices: number;
    totalTelemetryRecords: number;
    latestTelemetryCount: number;
    telemetryTrend7d: Array<{ date: string; count: number }>;
    recentTelemetryItems: Array<{
        id: number;
        deviceId: number;
        deviceName: string;
        metricCode: string;
        metricName: string;
        metricValue: string;
        updatedAt?: string;
    }>;
}
