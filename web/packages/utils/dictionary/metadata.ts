import { translate } from '@governance/i18n';

export type MetadataCollectionStrategyValue = 'FULL' | 'INCREMENTAL';
export type MetadataCollectionScopeValue = 'SCHEMA' | 'TABLE';
export type MetadataCollectionScheduleTypeValue = 'MANUAL' | 'CRON';

export const getMetadataStrategyText = (strategy?: string | null) => {
    switch (strategy) {
        case 'FULL':
            return translate('enum.metadataStrategy.full');
        case 'INCREMENTAL':
            return translate('enum.metadataStrategy.incremental');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return strategy;
    }
};

export const getMetadataScopeText = (scope?: string | null) => {
    switch (scope) {
        case 'SCHEMA':
            return translate('enum.metadataScope.schema');
        case 'TABLE':
            return translate('enum.metadataScope.table');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return scope;
    }
};

export const getMetadataScheduleTypeText = (scheduleType?: string | null) => {
    switch (scheduleType) {
        case 'MANUAL':
            return translate('enum.metadataSchedule.manual');
        case 'CRON':
            return translate('enum.metadataSchedule.cron');
        case undefined:
        case null:
        case '':
            return '-';
        default:
            return scheduleType;
    }
};

export const getMetadataStrategyOptions = () => [
    {
        label: translate('enum.metadataStrategy.full'),
        value: 'FULL',
    },
    {
        label: translate('enum.metadataStrategy.incremental'),
        value: 'INCREMENTAL',
    },
] as Array<{ label: string; value: MetadataCollectionStrategyValue }>;

export const getMetadataScopeOptions = () => [
    {
        label: translate('enum.metadataScope.schema'),
        value: 'SCHEMA',
    },
    {
        label: translate('enum.metadataScope.table'),
        value: 'TABLE',
    },
] as Array<{ label: string; value: MetadataCollectionScopeValue }>;

export const getMetadataScheduleTypeOptions = () => [
    {
        label: translate('enum.metadataSchedule.manual'),
        value: 'MANUAL',
    },
    {
        label: translate('enum.metadataSchedule.cron'),
        value: 'CRON',
    },
] as Array<{ label: string; value: MetadataCollectionScheduleTypeValue }>;
