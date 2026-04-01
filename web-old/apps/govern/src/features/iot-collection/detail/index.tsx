import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchIotCollectionTaskById,
    fetchIotTelemetryHistory,
    fetchIotTelemetryLatest,
    type IotCollectionTaskItem,
    type IotTelemetryHistoryItem,
    type IotTelemetryLatestItem,
} from '@governance/api';
import { useI18n } from '@governance/i18n';
import { IOT_COLLECTION_PATH, formatDateTime } from '@governance/utils';
import { Button, Card, Descriptions, Empty, Space, Table, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const IotCollectionDetailPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [data, setData] = useState<IotCollectionTaskItem | null>(null);
    const [latestTelemetry, setLatestTelemetry] = useState<IotTelemetryLatestItem[]>([]);
    const [historyTelemetry, setHistoryTelemetry] = useState<IotTelemetryHistoryItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const loadData = async () => {
        const taskId = Number(id);
        if (!Number.isFinite(taskId)) {
            messageApi.error(t('common.noData'));
            return;
        }

        setLoading(true);
        try {
            const taskResponse = await fetchIotCollectionTaskById(taskId);
            if (!taskResponse.success) {
                throw new Error(taskResponse.message || t('iotCollection.loadFailed'));
            }
            const task = taskResponse.data || null;
            setData(task);
            if (task?.deviceId) {
                const [latestResponse, historyResponse] = await Promise.all([
                    fetchIotTelemetryLatest(task.deviceId),
                    fetchIotTelemetryHistory({ deviceId: task.deviceId }),
                ]);
                if (!latestResponse.success) {
                    throw new Error(latestResponse.message || t('iotCollection.loadFailed'));
                }
                if (!historyResponse.success) {
                    throw new Error(historyResponse.message || t('iotCollection.loadFailed'));
                }
                setLatestTelemetry(latestResponse.data || []);
                setHistoryTelemetry(historyResponse.data || []);
            } else {
                setLatestTelemetry([]);
                setHistoryTelemetry([]);
            }
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotCollection.loadFailed'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, [id]);

    return (
        <div>
            {contextHolder}
            <Card
                title={t('iotCollection.pageTitle')}
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadData()}>
                            {t('common.refresh')}
                        </Button>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(IOT_COLLECTION_PATH)}>
                            {t('common.backToList')}
                        </Button>
                    </Space>
                }
                loading={loading}
            >
                {!data ? (
                    <Empty description={t('common.noData')} />
                ) : (
                    <Descriptions column={2} bordered>
                        <Descriptions.Item label={t('iotCollection.taskName')}>{data.taskName}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.deviceName')}>{data.deviceName}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.deviceCode')}>{data.deviceCode}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.productKey')}>{data.productKey || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.productName')}>{data.productName || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.collectionType')}>{data.collectionType}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.scheduleType')}>{data.scheduleType}</Descriptions.Item>
                        <Descriptions.Item label={t('common.enabled')}>{data.enabled ? 'ENABLED' : 'DISABLED'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.cronExpression')}>{data.cronExpression || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.pollIntervalSeconds')}>{data.pollIntervalSeconds || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.sourceType')}>{data.sourceType || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.dataFormat')}>{data.dataFormat || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotCollection.configJson')} span={2}>{data.configJson || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('common.description')} span={2}>{data.description || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('common.createdAt')}>{formatDateTime(data.createdAt)}</Descriptions.Item>
                        <Descriptions.Item label={t('common.updatedAt')}>{formatDateTime(data.updatedAt)}</Descriptions.Item>
                    </Descriptions>
                )}
            </Card>

            <Card title="Latest Telemetry" style={{ marginTop: 16 }}>
                <Table
                    rowKey="id"
                    dataSource={latestTelemetry}
                    pagination={false}
                    columns={[
                        { title: 'Metric Code', dataIndex: 'metricCode', key: 'metricCode' },
                        { title: 'Metric Name', dataIndex: 'metricName', key: 'metricName' },
                        { title: 'Value', dataIndex: 'metricValue', key: 'metricValue' },
                        { title: 'Collected At', dataIndex: 'collectedAt', key: 'collectedAt', render: (value) => formatDateTime(value) },
                    ]}
                />
            </Card>

            <Card title="History Telemetry" style={{ marginTop: 16 }}>
                <Table
                    rowKey="id"
                    dataSource={historyTelemetry}
                    pagination={{ pageSize: 10 }}
                    columns={[
                        { title: 'Metric Code', dataIndex: 'metricCode', key: 'metricCode' },
                        { title: 'Metric Name', dataIndex: 'metricName', key: 'metricName' },
                        { title: 'Value', dataIndex: 'metricValue', key: 'metricValue' },
                        { title: 'Collected At', dataIndex: 'collectedAt', key: 'collectedAt', render: (value) => formatDateTime(value) },
                    ]}
                />
            </Card>
        </div>
    );
};

export default IotCollectionDetailPage;
