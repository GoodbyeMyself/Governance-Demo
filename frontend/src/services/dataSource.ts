import { httpRequest } from '@/utils/http';

export type DataSourceType = 'DATABASE' | 'FILE_SYSTEM';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface DataSourceItem {
    id: number;
    name: string;
    type: DataSourceType;
    connectionUrl?: string;
    username?: string;
    description?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface DataSourcePayload {
    name: string;
    type: DataSourceType;
    connectionUrl?: string;
    username?: string;
    password?: string;
    description?: string;
}

export async function fetchDataSources() {
    return httpRequest<ApiResponse<DataSourceItem[]>>('/api/data-sources', {
        method: 'GET',
    });
}

export async function createDataSource(payload: DataSourcePayload) {
    return httpRequest<ApiResponse<DataSourceItem>>('/api/data-sources', {
        method: 'POST',
        requireBody: true,
        data: payload,
    });
}

export async function updateDataSource(id: number, payload: DataSourcePayload) {
    return httpRequest<ApiResponse<DataSourceItem>>(`/api/data-sources/${id}`, {
        method: 'PUT',
        requireBody: true,
        data: payload,
    });
}

export async function deleteDataSource(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/data-sources/${id}`, {
        method: 'DELETE',
    });
}
