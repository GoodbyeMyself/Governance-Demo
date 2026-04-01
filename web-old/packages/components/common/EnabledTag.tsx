import { useI18n } from '@governance/i18n';
import { Tag } from 'antd';

export interface EnabledTagProps {
    enabled?: boolean | null;
    enabledText?: string;
    disabledText?: string;
    fallback?: string;
}

export const EnabledTag: React.FC<EnabledTagProps> = ({
    enabled,
    enabledText,
    disabledText,
    fallback = '-',
}) => {
    const { t } = useI18n();

    if (typeof enabled !== 'boolean') {
        return <>{fallback}</>;
    }

    return (
        <Tag color={enabled ? 'success' : 'default'}>
            {enabled
                ? enabledText || t('common.enabled')
                : disabledText || t('common.disabled')}
        </Tag>
    );
};
