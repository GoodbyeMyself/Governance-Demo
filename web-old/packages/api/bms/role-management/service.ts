import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../../core';
import type {
    BmsRoleDefinition,
    BmsRoleDefinitionUpdatePayload,
} from './types';

const BMS_API_PREFIX = '/api/bms/role-definitions';

export const fetchBmsRoleDefinitions = async () =>
    httpRequest<ApiResponse<BmsRoleDefinition[]>>(BMS_API_PREFIX, {
        method: 'GET',
    });

export const updateBmsRoleDefinition = async (
    roleCode: string,
    payload: BmsRoleDefinitionUpdatePayload,
) =>
    httpRequest<ApiResponse<BmsRoleDefinition>>(`${BMS_API_PREFIX}/${roleCode}`, {
        method: 'PUT',
        requireBody: true,
        data: payload,
    });
