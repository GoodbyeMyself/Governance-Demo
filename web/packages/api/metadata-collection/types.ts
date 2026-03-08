import type { DataSourceType } from '../data-source';

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
