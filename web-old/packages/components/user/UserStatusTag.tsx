import type { BmsUserStatus } from '@governance/api';
import { getUserStatusColor, getUserStatusText } from '@governance/utils';
import { Tag } from 'antd';

export interface UserStatusTagProps {
    status?: BmsUserStatus | null;
    fallback?: string;
}

export const UserStatusTag: React.FC<UserStatusTagProps> = ({
    status,
    fallback = '-',
}) => {
    if (!status) {
        return <>{fallback}</>;
    }

    return <Tag color={getUserStatusColor(status)}>{getUserStatusText(status)}</Tag>;
};
