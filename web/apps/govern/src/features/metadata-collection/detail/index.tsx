import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchMetadataCollectionTaskDetail,
    type MetadataCollectionTaskItem,
} from '@governance/api';
import { DataSourceTypeTag, EnabledTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    METADATA_COLLECTION_PATH,
    formatDateTime,
    getMetadataScheduleTypeText,
    getMetadataScopeText,
    getMetadataStrategyText,
} from '@governance/utils';
import {
    Button,
    Card,
    Descriptions,
    Empty,
    Space,
    Spin,
    message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import styles from './index.module.less';

const MetadataCollectionDetailPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const params = useParams<{ id?: string }>();
    const taskId = useMemo(() => Number(params.id || 0), [params.id]);
    const [task, setTask] = useState<MetadataCollectionTaskItem | null>(null);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const loadDetail = async () => {
        if (!taskId || Number.isNaN(taskId)) {
            messageApi.error(t('metadata.invalidId'));
            return;
        }

        setLoading(true);
        try {
            const response = await fetchMetadataCollectionTaskDetail(taskId);
            if (!response.success || !response.data) {
                throw new Error(
                    response.message || t('metadata.detailLoadFailed'),
                );
            }
            setTask(response.data);
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('metadata.detailLoadFailed');
            messageApi.error(messageText);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadDetail();
    }, [taskId]);

    return (
        <div className={styles.page}>
            {contextHolder}

            <div className={styles.headerAction}>
                <Space>
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={() => void loadDetail()}
                    >
                        {t('common.refresh')}
                    </Button>
                    <Button
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate(METADATA_COLLECTION_PATH)}
                    >
                        {t('common.backToList')}
                    </Button>
                </Space>
            </div>

            <Card title={t('metadata.detailTitle')}>
                <Spin spinning={loading}>
                    {task ? (
                        <Descriptions column={2} bordered>
                            <Descriptions.Item
                                label={t('metadata.field.taskId')}
                            >
                                {task.id}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.taskName')}
                            >
                                {task.taskName}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.dataSource')}
                            >
                                {task.dataSourceName}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.dataSourceType')}
                            >
                                <DataSourceTypeTag type={task.dataSourceType} />
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.strategy')}
                            >
                                {getMetadataStrategyText(task.strategy)}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.scope')}
                            >
                                {getMetadataScopeText(task.scope)}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.scheduleType')}
                            >
                                {getMetadataScheduleTypeText(task.scheduleType)}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.cronExpression')}
                            >
                                {task.cronExpression || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.targetPattern')}
                                span={2}
                            >
                                {task.targetPattern || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label={t('common.status')}>
                                <EnabledTag enabled={task.enabled} />
                            </Descriptions.Item>
                            <Descriptions.Item label={t('common.updatedAt')}>
                                {formatDateTime(task.updatedAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label={t('common.createdAt')}>
                                {formatDateTime(task.createdAt)}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('common.description')}
                            >
                                {task.description || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item
                                label={t('metadata.field.configJson')}
                                span={2}
                            >
                                {task.configJson ? (
                                    <pre className={styles.jsonBlock}>
                                        {task.configJson}
                                    </pre>
                                ) : (
                                    '-'
                                )}
                            </Descriptions.Item>
                        </Descriptions>
                    ) : (
                        <Empty description={t('common.noData')} />
                    )}
                </Spin>
            </Card>
        </div>
    );
};

export default MetadataCollectionDetailPage;
