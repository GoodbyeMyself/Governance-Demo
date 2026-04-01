import type { BmsUserRole } from '../user-management/types';

export interface BmsRoleDefinition {
    id: number;
    roleCode: BmsUserRole | string;
    roleName: string;
    editable: boolean;
    userCount: number;
}

export interface BmsRoleDefinitionUpdatePayload {
    roleName: string;
}
