import { request } from '@umijs/max';
import type { ApiResponse } from './dataSource';

export interface WorkbenchRecentDataSourceItem {
    id: number;
    name: string;
    type: 'DATABASE' | 'FILE_SYSTEM';
    updatedAt?: string;
}

export interface WorkbenchRecentMetadataTaskItem {
    id: number;
    taskName: string;
    dataSourceName: string;
    strategy: 'FULL' | 'INCREMENTAL';
    scheduleType: 'MANUAL' | 'CRON';
    enabled: boolean;
    updatedAt?: string;
}

export interface WorkbenchDailyTrendItem {
    date: string;
    count: number;
}

export interface WorkbenchOverview {
    totalDataSources: number;
    databaseDataSourceCount: number;
    fileSystemDataSourceCount: number;
    totalMetadataTasks: number;
    enabledMetadataTaskCount: number;
    cronMetadataTaskCount: number;
    fullMetadataTaskCount: number;
    incrementalMetadataTaskCount: number;
    dataSourceUpdateTrend7d: WorkbenchDailyTrendItem[];
    metadataTaskUpdateTrend7d: WorkbenchDailyTrendItem[];
    recentDataSources: WorkbenchRecentDataSourceItem[];
    recentMetadataTasks: WorkbenchRecentMetadataTaskItem[];
}

export async function fetchWorkbenchOverview() {
    return request<ApiResponse<WorkbenchOverview>>('/api/workbench/overview', {
        method: 'GET',
    });
}
