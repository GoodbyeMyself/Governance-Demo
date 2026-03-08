import { translate } from '@governance/i18n';

export type DataSourceTypeValue = 'DATABASE' | 'FILE_SYSTEM';

const dataSourceTypeColorMap: Record<DataSourceTypeValue, string> = {
    DATABASE: 'blue',
    FILE_SYSTEM: 'green',
};

export const getDataSourceTypeText = (type?: string | null) => {
    switch (type) {
        case 'DATABASE':
            return translate('enum.dataSourceType.database');
        case 'FILE_SYSTEM':
            return translate('enum.dataSourceType.fileSystem');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return type;
    }
};

export const getDataSourceTypeColor = (type?: string | null) => {
    if (!type) return 'default';
    return dataSourceTypeColorMap[type as DataSourceTypeValue] || 'default';
};

export const getDataSourceTypeOptions = () => [
    {
        label: translate('enum.dataSourceType.database'),
        value: 'DATABASE',
    },
    {
        label: translate('enum.dataSourceType.fileSystem'),
        value: 'FILE_SYSTEM',
    },
] as Array<{ label: string; value: DataSourceTypeValue }>;
