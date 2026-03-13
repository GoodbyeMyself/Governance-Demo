import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchIotDeviceById,
    fetchIotTelemetryLatest,
    type IotDeviceItem,
    type IotTelemetryLatestItem,
} from '@governance/api';
import { useI18n } from '@governance/i18n';
import { IOT_DEVICE_PATH, formatDateTime } from '@governance/utils';
import { Button, Card, Descriptions, Empty, Space, Table, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const IotDeviceDetailPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [data, setData] = useState<IotDeviceItem | null>(null);
    const [latestTelemetry, setLatestTelemetry] = useState<IotTelemetryLatestItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const loadData = async () => {
        const deviceId = Number(id);
        if (!Number.isFinite(deviceId)) {
            messageApi.error(t('common.noData'));
            return;
        }

        setLoading(true);
        try {
            const [deviceResponse, telemetryResponse] = await Promise.all([
                fetchIotDeviceById(deviceId),
                fetchIotTelemetryLatest(deviceId),
            ]);
            if (!deviceResponse.success) {
                throw new Error(deviceResponse.message || t('iotDevice.loadFailed'));
            }
            if (!telemetryResponse.success) {
                throw new Error(telemetryResponse.message || t('iotCollection.loadFailed'));
            }
            setData(deviceResponse.data || null);
            setLatestTelemetry(telemetryResponse.data || []);
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotDevice.loadFailed'));
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
                title={t('iotDevice.pageTitle')}
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadData()}>
                            {t('common.refresh')}
                        </Button>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(IOT_DEVICE_PATH)}>
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
                        <Descriptions.Item label={t('iotDevice.deviceCode')}>{data.deviceCode}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.deviceName')}>{data.deviceName}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.productKey')}>{data.productKey || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.productName')}>{data.productName || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.deviceType')}>{data.deviceType}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.protocolType')}>{data.protocolType}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.endpoint')}>{data.endpoint || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.connectionHost')}>{data.connectionHost || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.connectionPort')}>{data.connectionPort || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.topicOrPath')}>{data.topicOrPath || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.username')}>{data.username || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.enabled')}>{data.enabled ? 'ENABLED' : 'DISABLED'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.onlineStatus')}>{data.onlineStatus || '-'}</Descriptions.Item>
                        <Descriptions.Item label={t('iotDevice.status')}>{data.status}</Descriptions.Item>
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
                        { title: 'Unit', dataIndex: 'unit', key: 'unit', render: (value) => value || '-' },
                        { title: 'Quality', dataIndex: 'quality', key: 'quality', render: (value) => value || '-' },
                        { title: 'Collected At', dataIndex: 'collectedAt', key: 'collectedAt', render: (value) => formatDateTime(value) },
                    ]}
                />
            </Card>
        </div>
    );
};

export default IotDeviceDetailPage;
