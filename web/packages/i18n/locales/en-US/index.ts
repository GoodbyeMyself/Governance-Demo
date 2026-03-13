import type { LocaleMessages } from '../../types';
import governMessages from './apps/govern';
import portalMessages from './apps/portal';
import accessMessages from './modules/access';
import assistantMessages from './modules/assistant';
import authMessages from './modules/auth';
import dashboardMessages from './modules/dashboard';
import dataMetadataMessages from './modules/data-metadata';
import dataSourceMessages from './modules/data-source';
import iotMessages from './modules/iot';
import portalDemoMessages from './modules/portal-demo';
import profileMessages from './modules/profile';
import roleManagementMessages from './modules/role-management';
import userManagementMessages from './modules/user-management';
import commonMessages from './shared/common';
import enumMessages from './shared/enums';
import httpMessages from './shared/http';
import languageMessages from './shared/language';
import navigationMessages from './shared/navigation';

const enUS: LocaleMessages = {
    ...commonMessages,
    ...languageMessages,
    ...navigationMessages,
    ...enumMessages,
    ...httpMessages,
    ...authMessages,
    ...dashboardMessages,
    ...dataSourceMessages,
    ...iotMessages,
    ...dataMetadataMessages,
    ...portalDemoMessages,
    ...profileMessages,
    ...roleManagementMessages,
    ...userManagementMessages,
    ...accessMessages,
    ...assistantMessages,
    ...governMessages,
    ...portalMessages,
};

export default enUS;
