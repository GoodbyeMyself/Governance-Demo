import type { LocaleMessages } from '../../../types';

const enumMessages: LocaleMessages = {
    'enum.dataSourceType.database': '数据库',
    'enum.dataSourceType.fileSystem': '文件系统',
    'enum.metadataSchedule.cron': 'Cron 定时',
    'enum.metadataSchedule.manual': '手动触发',
    'enum.metadataScope.schema': 'Schema 级',
    'enum.metadataScope.table': '表级',
    'enum.metadataStrategy.full': '全量采集',
    'enum.metadataStrategy.incremental': '增量采集',
    'enum.userRole.admin': '管理员',
    'enum.userRole.user': '普通用户',
    'enum.userStatus.disabled': '禁用',
    'enum.userStatus.enabled': '启用',
};

export default enumMessages;
