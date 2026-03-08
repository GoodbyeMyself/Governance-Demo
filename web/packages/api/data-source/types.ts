export type DataSourceType = 'DATABASE' | 'FILE_SYSTEM';

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
