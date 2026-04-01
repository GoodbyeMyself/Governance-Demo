import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../core';
import type { DataSourceItem, DataSourcePayload } from './types';

export async function fetchDataSources() {
    return httpRequest<ApiResponse<DataSourceItem[]>>('/api/data-source', {
        method: 'GET',
    });
}

export async function createDataSource(payload: DataSourcePayload) {
    return httpRequest<ApiResponse<DataSourceItem>>('/api/data-source', {
        method: 'POST',
        requireBody: true,
        data: payload,
    });
}

export async function updateDataSource(id: number, payload: DataSourcePayload) {
    return httpRequest<ApiResponse<DataSourceItem>>(`/api/data-source/${id}`, {
        method: 'PUT',
        requireBody: true,
        data: payload,
    });
}

export async function deleteDataSource(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/data-source/${id}`, {
        method: 'DELETE',
    });
}
