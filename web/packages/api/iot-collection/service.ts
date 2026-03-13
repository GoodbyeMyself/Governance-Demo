import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../core';
import type {
    IotCollectionTaskItem,
    IotCollectionTaskPayload,
    IotTelemetryHistoryItem,
    IotTelemetryLatestItem,
    IotTelemetryOverview,
} from './types';

export async function fetchIotCollectionTasks() {
    return httpRequest<ApiResponse<IotCollectionTaskItem[]>>('/api/iot-collection/tasks', {
        method: 'GET',
    });
}

export async function fetchIotCollectionTaskById(id: number) {
    return httpRequest<ApiResponse<IotCollectionTaskItem>>(`/api/iot-collection/tasks/${id}`, {
        method: 'GET',
    });
}

export async function createIotCollectionTask(payload: IotCollectionTaskPayload) {
    return httpRequest<ApiResponse<IotCollectionTaskItem>>('/api/iot-collection/tasks', {
        method: 'POST',
        requireBody: true,
        data: payload,
    });
}

export async function updateIotCollectionTask(id: number, payload: IotCollectionTaskPayload) {
    return httpRequest<ApiResponse<IotCollectionTaskItem>>(`/api/iot-collection/tasks/${id}`, {
        method: 'PUT',
        requireBody: true,
        data: payload,
    });
}

export async function deleteIotCollectionTask(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/iot-collection/tasks/${id}`, {
        method: 'DELETE',
    });
}

export async function fetchIotTelemetryLatest(deviceId: number) {
    return httpRequest<ApiResponse<IotTelemetryLatestItem[]>>(`/api/iot-collection/telemetry/latest/${deviceId}`, {
        method: 'GET',
    });
}

export async function fetchIotTelemetryHistory(params: {
    deviceId: number;
    metricCode?: string;
    startTime?: string;
    endTime?: string;
}) {
    const query = new URLSearchParams();
    query.set('deviceId', String(params.deviceId));
    if (params.metricCode) query.set('metricCode', params.metricCode);
    if (params.startTime) query.set('startTime', params.startTime);
    if (params.endTime) query.set('endTime', params.endTime);

    return httpRequest<ApiResponse<IotTelemetryHistoryItem[]>>(`/api/iot-collection/telemetry/history?${query.toString()}`, {
        method: 'GET',
    });
}

export async function fetchIotTelemetryOverview() {
    return httpRequest<ApiResponse<IotTelemetryOverview>>('/api/iot-collection/telemetry/overview', {
        method: 'GET',
    });
}
