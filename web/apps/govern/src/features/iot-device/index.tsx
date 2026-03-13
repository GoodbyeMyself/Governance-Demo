import {
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import {
    createIotDevice,
    deleteIotDevice,
    fetchIotDevices,
    updateIotDevice,
    type IotDeviceItem,
    type IotDevicePayload,
} from '@governance/api';
import { useI18n } from '@governance/i18n';
import { buildIotDeviceDetailPath, formatDateTime } from '@governance/utils';
import {
    Button,
    Card,
    Empty,
    Form,
    Input,
    Modal,
    Popconfirm,
    Space,
    Switch,
    Table,
    Tag,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const IotDevicePage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [form] = Form.useForm<IotDevicePayload>();
    const [data, setData] = useState<IotDeviceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingItem, setEditingItem] = useState<IotDeviceItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const isEditMode = Boolean(editingItem);
    const enabled = Form.useWatch('enabled', form);

    const loadData = async () => {
        setLoading(true);
        try {
            const response = await fetchIotDevices();
            if (!response.success) {
                throw new Error(response.message || t('iotDevice.loadFailed'));
            }
            setData(response.data || []);
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotDevice.loadFailed'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const openCreateModal = () => {
        setEditingItem(null);
        form.resetFields();
        form.setFieldsValue({
            status: 'ENABLED',
            enabled: true,
            onlineStatus: 'OFFLINE',
            protocolType: 'MQTT',
            deviceType: 'SENSOR',
        });
        setOpen(true);
    };

    const openEditModal = (record: IotDeviceItem) => {
        setEditingItem(record);
        form.setFieldsValue({
            deviceCode: record.deviceCode,
            deviceName: record.deviceName,
            productKey: record.productKey,
            productName: record.productName,
            deviceType: record.deviceType,
            protocolType: record.protocolType,
            endpoint: record.endpoint,
            connectionHost: record.connectionHost,
            connectionPort: record.connectionPort,
            topicOrPath: record.topicOrPath,
            username: record.username,
            enabled: record.enabled,
            onlineStatus: record.onlineStatus,
            status: record.status,
            description: record.description,
        });
        setOpen(true);
    };

    const closeModal = () => {
        setOpen(false);
        setEditingItem(null);
        form.resetFields();
    };

    const submitDevice = async () => {
        try {
            const values = await form.validateFields();
            if (!values.enabled && (values.onlineStatus === 'ONLINE' || values.status === 'ENABLED')) {
                messageApi.error(t('iotDevice.onlineEnabledConflict'));
                return;
            }
            const payload: IotDevicePayload = {
                deviceCode: values.deviceCode?.trim() || '',
                deviceName: values.deviceName?.trim() || '',
                productKey: values.productKey?.trim() || undefined,
                productName: values.productName?.trim() || undefined,
                deviceType: values.deviceType?.trim() || '',
                protocolType: values.protocolType?.trim() || '',
                endpoint: values.endpoint?.trim() || undefined,
                connectionHost: values.connectionHost?.trim() || undefined,
                connectionPort: values.connectionPort,
                topicOrPath: values.topicOrPath?.trim() || undefined,
                username: values.username?.trim() || undefined,
                passwordOrSecret: values.passwordOrSecret?.trim() || undefined,
                enabled: Boolean(values.enabled),
                onlineStatus: values.onlineStatus?.trim() || 'OFFLINE',
                status: values.status?.trim() || '',
                description: values.description?.trim() || undefined,
            };

            setSaving(true);
            if (isEditMode && editingItem) {
                const response = await updateIotDevice(editingItem.id, payload);
                if (!response.success) {
                    throw new Error(response.message || t('iotDevice.updateFailed'));
                }
                messageApi.success(t('iotDevice.updateSuccess'));
            } else {
                const response = await createIotDevice(payload);
                if (!response.success) {
                    throw new Error(response.message || t('iotDevice.createFailed'));
                }
                messageApi.success(t('iotDevice.createSuccess'));
            }

            closeModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }
            messageApi.error(error instanceof Error ? error.message : t('iotDevice.saveFailed'));
        } finally {
            setSaving(false);
        }
    };

    const removeDevice = async (id: number) => {
        try {
            const response = await deleteIotDevice(id);
            if (!response.success) {
                throw new Error(response.message || t('iotDevice.deleteFailed'));
            }
            messageApi.success(t('iotDevice.deleteSuccess'));
            await loadData();
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotDevice.deleteFailed'));
        }
    };

    const columns: ColumnsType<IotDeviceItem> = [
        { title: t('iotDevice.deviceCode'), dataIndex: 'deviceCode', key: 'deviceCode' },
        { title: t('iotDevice.deviceName'), dataIndex: 'deviceName', key: 'deviceName' },
        { title: t('iotDevice.productName'), dataIndex: 'productName', key: 'productName', render: (value) => value || '-' },
        { title: t('iotDevice.deviceType'), dataIndex: 'deviceType', key: 'deviceType' },
        { title: t('iotDevice.protocolType'), dataIndex: 'protocolType', key: 'protocolType' },
        { title: t('iotDevice.connectionHost'), dataIndex: 'connectionHost', key: 'connectionHost', render: (value) => value || '-' },
        {
            title: t('iotDevice.onlineStatus'),
            dataIndex: 'onlineStatus',
            key: 'onlineStatus',
            render: (value) => <Tag color={value === 'ONLINE' ? 'green' : 'default'}>{value || 'OFFLINE'}</Tag>,
        },
        {
            title: t('iotDevice.enabled'),
            dataIndex: 'enabled',
            key: 'enabled',
            render: (value) => <Tag color={value ? 'green' : 'default'}>{value ? 'ENABLED' : 'DISABLED'}</Tag>,
        },
        {
            title: t('iotDevice.status'),
            dataIndex: 'status',
            key: 'status',
            render: (value) => <Tag color={value === 'ENABLED' ? 'green' : 'default'}>{value}</Tag>,
        },
        {
            title: t('common.updatedAt'),
            dataIndex: 'updatedAt',
            key: 'updatedAt',
            render: (value) => formatDateTime(value),
        },
        {
            title: t('common.actions'),
            key: 'actions',
            render: (_, record) => (
                <Space>
                    <Button icon={<EyeOutlined />} onClick={() => navigate(buildIotDeviceDetailPath(record.id))} />
                    <Button icon={<EditOutlined />} onClick={() => openEditModal(record)} />
                    <Popconfirm title={t('iotDevice.confirmDelete')} onConfirm={() => void removeDevice(record.id)}>
                        <Button danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                </Space>
            ),
        },
    ];

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
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                            {t('iotDevice.createButton')}
                        </Button>
                    </Space>
                }
            >
                {data.length === 0 && !loading ? (
                    <Empty description={t('iotDevice.empty')} />
                ) : (
                    <Table rowKey="id" dataSource={data} columns={columns} loading={loading} pagination={{ pageSize: 10 }} />
                )}
            </Card>

            <Modal
                title={isEditMode ? t('common.edit') : t('iotDevice.createButton')}
                open={open}
                onCancel={closeModal}
                onOk={() => void submitDevice()}
                confirmLoading={saving}
                destroyOnHidden
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="deviceCode" label={t('iotDevice.deviceCode')} rules={[{ required: true, message: t('iotDevice.deviceCodeRequired') }]}>
                        <Input placeholder="DEVICE_001" />
                    </Form.Item>
                    <Form.Item name="deviceName" label={t('iotDevice.deviceName')} rules={[{ required: true, message: t('iotDevice.deviceNameRequired') }]}>
                        <Input placeholder="Sensor-01" />
                    </Form.Item>
                    <Form.Item name="productKey" label={t('iotDevice.productKey')}>
                        <Input placeholder="TEMP_SENSOR_V1" />
                    </Form.Item>
                    <Form.Item name="productName" label={t('iotDevice.productName')}>
                        <Input placeholder="温度传感器产品" />
                    </Form.Item>
                    <Form.Item name="deviceType" label={t('iotDevice.deviceType')} rules={[{ required: true, message: t('iotDevice.deviceTypeRequired') }]}>
                        <Input placeholder="SENSOR" />
                    </Form.Item>
                    <Form.Item name="protocolType" label={t('iotDevice.protocolType')} rules={[{ required: true, message: t('iotDevice.protocolTypeRequired') }]}>
                        <Input placeholder="MQTT" />
                    </Form.Item>
                    <Form.Item name="endpoint" label={t('iotDevice.endpoint')}>
                        <Input placeholder={t('iotDevice.endpointPlaceholder')} />
                    </Form.Item>
                    <Form.Item name="connectionHost" label={t('iotDevice.connectionHost')}>
                        <Input placeholder="127.0.0.1" />
                    </Form.Item>
                    <Form.Item name="connectionPort" label={t('iotDevice.connectionPort')}>
                        <Input type="number" placeholder="1883" />
                    </Form.Item>
                    <Form.Item name="topicOrPath" label={t('iotDevice.topicOrPath')}>
                        <Input placeholder="/device/001/telemetry" />
                    </Form.Item>
                    <Form.Item name="username" label={t('iotDevice.username')}>
                        <Input placeholder="device_user" />
                    </Form.Item>
                    <Form.Item name="passwordOrSecret" label={isEditMode ? t('iotDevice.passwordKeepHint') : t('iotDevice.passwordOrSecret')}>
                        <Input.Password placeholder="secret" />
                    </Form.Item>
                    <Form.Item name="enabled" label={t('iotDevice.enabled')} valuePropName="checked">
                        <Switch />
                    </Form.Item>
                    <Form.Item name="onlineStatus" label={t('iotDevice.onlineStatus')}>
                        <Input placeholder="OFFLINE" disabled={!enabled} />
                    </Form.Item>
                    <Form.Item name="status" label={t('iotDevice.status')} rules={[{ required: true, message: t('iotDevice.statusRequired') }]}>
                        <Input placeholder="ENABLED" disabled={!enabled} />
                    </Form.Item>
                    <Form.Item name="description" label={t('common.description')}>
                        <Input.TextArea rows={3} placeholder={t('iotDevice.descriptionPlaceholder')} />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default IotDevicePage;
