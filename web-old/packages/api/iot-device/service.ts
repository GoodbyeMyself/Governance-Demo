import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../core';
import type { IotDeviceItem, IotDevicePayload } from './types';

export async function fetchIotDevices() {
    return httpRequest<ApiResponse<IotDeviceItem[]>>('/api/iot-device', {
        method: 'GET',
    });
}

export async function fetchIotDeviceById(id: number) {
    return httpRequest<ApiResponse<IotDeviceItem>>(`/api/iot-device/${id}`, {
        method: 'GET',
    });
}

export async function createIotDevice(payload: IotDevicePayload) {
    return httpRequest<ApiResponse<IotDeviceItem>>('/api/iot-device', {
        method: 'POST',
        requireBody: true,
        data: payload,
    });
}

export async function updateIotDevice(id: number, payload: IotDevicePayload) {
    return httpRequest<ApiResponse<IotDeviceItem>>(`/api/iot-device/${id}`, {
        method: 'PUT',
        requireBody: true,
        data: payload,
    });
}

export async function deleteIotDevice(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/iot-device/${id}`, {
        method: 'DELETE',
    });
}
