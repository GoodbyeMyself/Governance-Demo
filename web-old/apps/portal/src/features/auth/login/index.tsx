import { useI18n } from '@governance/i18n';
import { AuthCenterAuthPage } from '@governance/ui';
import { HOME_PATH } from '@governance/utils';

const LoginPage = () => {
    const { t } = useI18n();

    return (
        <AuthCenterAuthPage
            title={t('portal.auth.title')}
            subtitle={t('portal.auth.subtitle')}
            defaultRedirect={HOME_PATH}
        />
    );
};

export default LoginPage;
