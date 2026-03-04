import { request } from '@umijs/max';
import type { ApiResponse, DataSourceType } from './dataSource';

export type MetadataCollectionStrategy = 'FULL' | 'INCREMENTAL';
export type MetadataCollectionScope = 'SCHEMA' | 'TABLE';
export type MetadataCollectionScheduleType = 'MANUAL' | 'CRON';

export interface MetadataCollectionTaskItem {
    id: number;
    taskName: string;
    dataSourceId: number;
    dataSourceName: string;
    dataSourceType: DataSourceType;
    strategy: MetadataCollectionStrategy;
    scope: MetadataCollectionScope;
    targetPattern?: string;
    scheduleType: MetadataCollectionScheduleType;
    cronExpression?: string;
    configJson?: string;
    description?: string;
    enabled: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface MetadataCollectionTaskPayload {
    taskName: string;
    dataSourceId: number;
    strategy: MetadataCollectionStrategy;
    scope: MetadataCollectionScope;
    targetPattern?: string;
    scheduleType: MetadataCollectionScheduleType;
    cronExpression?: string;
    configJson?: string;
    description?: string;
    enabled: boolean;
}

export async function fetchMetadataCollectionTasks() {
    return request<ApiResponse<MetadataCollectionTaskItem[]>>(
        '/api/metadata-collection-tasks',
        {
            method: 'GET',
        },
    );
}

export async function fetchMetadataCollectionTaskDetail(id: number) {
    return request<ApiResponse<MetadataCollectionTaskItem>>(
        `/api/metadata-collection-tasks/${id}`,
        {
            method: 'GET',
        },
    );
}

export async function createMetadataCollectionTask(
    payload: MetadataCollectionTaskPayload,
) {
    return request<ApiResponse<MetadataCollectionTaskItem>>(
        '/api/metadata-collection-tasks',
        {
            method: 'POST',
            data: payload,
        },
    );
}

export async function updateMetadataCollectionTask(
    id: number,
    payload: MetadataCollectionTaskPayload,
) {
    return request<ApiResponse<MetadataCollectionTaskItem>>(
        `/api/metadata-collection-tasks/${id}`,
        {
            method: 'PUT',
            data: payload,
        },
    );
}

export async function deleteMetadataCollectionTask(id: number) {
    return request<ApiResponse<null>>(`/api/metadata-collection-tasks/${id}`, {
        method: 'DELETE',
    });
}
