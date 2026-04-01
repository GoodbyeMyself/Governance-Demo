import { httpRequest } from '@governance/utils';
import type { ApiResponse } from '../core';
import type { WorkbenchOverview } from './types';

export async function fetchWorkbenchOverview() {
    return httpRequest<ApiResponse<WorkbenchOverview>>(
        '/api/data-metadata/workbench/overview',
        {
            method: 'GET',
        },
    );
}
