import type { ApiResponse, DataSourceType } from './dataSource';
import { httpRequest } from '@/utils/http';

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
    return httpRequest<ApiResponse<MetadataCollectionTaskItem[]>>(
        '/api/metadata-collection-tasks',
        {
            method: 'GET',
        },
    );
}

export async function fetchMetadataCollectionTaskDetail(id: number) {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem>>(
        `/api/metadata-collection-tasks/${id}`,
        {
            method: 'GET',
        },
    );
}

export async function createMetadataCollectionTask(
    payload: MetadataCollectionTaskPayload,
) {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem>>(
        '/api/metadata-collection-tasks',
        {
            method: 'POST',
            requireBody: true,
            data: payload,
        },
    );
}

export async function updateMetadataCollectionTask(
    id: number,
    payload: MetadataCollectionTaskPayload,
) {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem>>(
        `/api/metadata-collection-tasks/${id}`,
        {
            method: 'PUT',
            requireBody: true,
            data: payload,
        },
    );
}

export async function deleteMetadataCollectionTask(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/metadata-collection-tasks/${id}`, {
        method: 'DELETE',
    });
}
