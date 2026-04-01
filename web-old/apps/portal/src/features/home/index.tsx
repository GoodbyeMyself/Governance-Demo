import { useI18n } from '@governance/i18n';
import { WorkbenchOverviewDashboard } from '@governance/ui';
import styles from './index.module.less';

const HomePage: React.FC = () => {
    const { t } = useI18n();

    return (
        <WorkbenchOverviewDashboard
            styles={styles}
            headerTitle={t('dashboard.portal.title')}
            headerDescription={t('dashboard.portal.description')}
        />
    );
};

export default HomePage;
