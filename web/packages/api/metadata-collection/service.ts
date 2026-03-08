import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../core';
import type {
    MetadataCollectionTaskItem,
    MetadataCollectionTaskPayload,
} from './types';

export async function fetchMetadataCollectionTasks() {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem[]>>(
        '/api/data-metadata/tasks',
        {
            method: 'GET',
        },
    );
}

export async function fetchMetadataCollectionTaskDetail(id: number) {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem>>(
        `/api/data-metadata/tasks/${id}`,
        {
            method: 'GET',
        },
    );
}

export async function createMetadataCollectionTask(
    payload: MetadataCollectionTaskPayload,
) {
    return httpRequest<ApiResponse<MetadataCollectionTaskItem>>(
        '/api/data-metadata/tasks',
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
        `/api/data-metadata/tasks/${id}`,
        {
            method: 'PUT',
            requireBody: true,
            data: payload,
        },
    );
}

export async function deleteMetadataCollectionTask(id: number) {
    return httpRequest<ApiResponse<null>>(`/api/data-metadata/tasks/${id}`, {
        method: 'DELETE',
    });
}
