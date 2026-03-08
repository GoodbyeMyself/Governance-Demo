import { useI18n } from '@governance/i18n';
import { WorkbenchOverviewDashboard } from '@governance/ui';
import { DATA_SOURCE_PATH, METADATA_COLLECTION_PATH } from '@governance/utils';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

const HomePage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();

    return (
        <WorkbenchOverviewDashboard
            styles={styles}
            headerTitle={t('dashboard.govern.title')}
            headerDescription={t('dashboard.govern.description')}
            showQuickActions={true}
            onDataSourceClick={() => navigate(DATA_SOURCE_PATH)}
            onMetadataCollectionClick={() => navigate(METADATA_COLLECTION_PATH)}
        />
    );
};

export default HomePage;
