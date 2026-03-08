import { useI18n } from '@governance/i18n';
import type { LocaleCode } from '@governance/i18n';
import { Select } from 'antd';

export interface LanguageSelectProps {
    size?: 'small' | 'middle' | 'large';
    bordered?: boolean;
}

export const LanguageSelect: React.FC<LanguageSelectProps> = ({
    size = 'middle',
    bordered = true,
}) => {
    const { locale, setLocale, t } = useI18n();

    return (
        <Select<LocaleCode>
            value={locale}
            size={size}
            variant={bordered ? 'outlined' : 'borderless'}
            style={{ minWidth: 108 }}
            options={[
                {
                    label: t('language.zh-CN'),
                    value: 'zh-CN',
                },
                {
                    label: t('language.en-US'),
                    value: 'en-US',
                },
            ]}
            onChange={(value) => setLocale(value)}
        />
    );
};
