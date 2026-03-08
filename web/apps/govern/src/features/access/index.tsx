import type { AuthCenterUserProfile } from '@governance/api';
import { useI18n } from '@governance/i18n';
import { getStoredUser } from '@governance/utils';
import { Button } from 'antd';

const AccessPage: React.FC = () => {
    const { t } = useI18n();
    const canSeeAdmin =
        getStoredUser<AuthCenterUserProfile>()?.role === 'ADMIN';

    return (
        <div
            style={{
                minHeight: '400px',
                backgroundColor: '#fff',
                padding: 16,
            }}
        >
            {canSeeAdmin ? (
                <Button>{t('access.adminOnlyButton')}</Button>
            ) : (
                <span>{t('access.noAdminPermission')}</span>
            )}
        </div>
    );
};

export default AccessPage;
