import type { LocaleMessages } from '../../../types';

const enumMessages: LocaleMessages = {
    'enum.dataSourceType.database': 'Database',
    'enum.dataSourceType.fileSystem': 'File System',
    'enum.metadataSchedule.cron': 'Cron',
    'enum.metadataSchedule.manual': 'Manual',
    'enum.metadataScope.schema': 'Schema Level',
    'enum.metadataScope.table': 'Table Level',
    'enum.metadataStrategy.full': 'Full Collection',
    'enum.metadataStrategy.incremental': 'Incremental Collection',
    'enum.userRole.admin': 'Admin',
    'enum.userRole.user': 'User',
    'enum.userStatus.disabled': 'Disabled',
    'enum.userStatus.enabled': 'Enabled',
};

export default enumMessages;
