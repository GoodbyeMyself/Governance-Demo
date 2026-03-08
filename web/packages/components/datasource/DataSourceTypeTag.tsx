import type { DataSourceType } from '@governance/api';
import {
    getDataSourceTypeColor,
    getDataSourceTypeText,
} from '@governance/utils';
import { Tag } from 'antd';

export interface DataSourceTypeTagProps {
    type?: DataSourceType | null;
    fallback?: string;
}

export const DataSourceTypeTag: React.FC<DataSourceTypeTagProps> = ({
    type,
    fallback = '-',
}) => {
    if (!type) {
        return <>{fallback}</>;
    }

    return <Tag color={getDataSourceTypeColor(type)}>{getDataSourceTypeText(type)}</Tag>;
};
