import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../../core';
import type {
    BmsUserProfile,
    BmsUserRole,
    BmsUserRoleUpdatePayload,
} from './types';

const BMS_API_PREFIX = '/api/bms';

export const fetchBmsUsers = async () =>
    httpRequest<ApiResponse<BmsUserProfile[]>>(`${BMS_API_PREFIX}/users`, {
        method: 'GET',
    });

export const updateBmsUserRole = async (
    userId: number,
    payload: BmsUserRoleUpdatePayload,
) =>
    httpRequest<ApiResponse<BmsUserProfile>>(
        `${BMS_API_PREFIX}/users/${userId}/role`,
        {
            method: 'PUT',
            requireBody: true,
            data: payload,
        },
    );

export const fetchBmsRoles = async () =>
    httpRequest<ApiResponse<BmsUserRole[]>>(`${BMS_API_PREFIX}/roles`, {
        method: 'GET',
    });

export const fetchBmsPermissions = async () =>
    httpRequest<ApiResponse<string[]>>(`${BMS_API_PREFIX}/permissions`, {
        method: 'GET',
    });
