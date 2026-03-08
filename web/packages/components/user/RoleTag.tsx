import type { BmsUserRole } from '@governance/api';
import { getUserRoleColor, getUserRoleText } from '@governance/utils';
import { Tag } from 'antd';

export interface RoleTagProps {
    role?: BmsUserRole | null;
    fallback?: string;
}

export const RoleTag: React.FC<RoleTagProps> = ({
    role,
    fallback = '-',
}) => {
    if (!role) {
        return <>{fallback}</>;
    }

    return <Tag color={getUserRoleColor(role)}>{getUserRoleText(role)}</Tag>;
};
