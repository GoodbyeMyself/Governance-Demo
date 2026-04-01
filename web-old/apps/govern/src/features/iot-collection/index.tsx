import {
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import {
    createIotCollectionTask,
    deleteIotCollectionTask,
    fetchIotCollectionTasks,
    fetchIotDevices,
    updateIotCollectionTask,
    type IotCollectionTaskItem,
    type IotCollectionTaskPayload,
    type IotDeviceItem,
} from '@governance/api';
import { useI18n } from '@governance/i18n';
import { buildIotCollectionDetailPath, formatDateTime } from '@governance/utils';
import {
    Button,
    Card,
    Empty,
    Form,
    Input,
    Modal,
    Popconfirm,
    Select,
    Space,
    Switch,
    Table,
    Tag,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const IotCollectionPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [form] = Form.useForm<IotCollectionTaskPayload>();
    const [data, setData] = useState<IotCollectionTaskItem[]>([]);
    const [devices, setDevices] = useState<IotDeviceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingItem, setEditingItem] = useState<IotCollectionTaskItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const isEditMode = Boolean(editingItem);
    const scheduleType = Form.useWatch('scheduleType', form);

    const deviceOptions = useMemo(
        () => devices.map((item) => ({ label: `${item.deviceName} (${item.deviceCode})`, value: item.id })),
        [devices],
    );

    const loadData = async () => {
        setLoading(true);
        try {
            const [taskResponse, deviceResponse] = await Promise.all([
                fetchIotCollectionTasks(),
                fetchIotDevices(),
            ]);
            if (!taskResponse.success) {
                throw new Error(taskResponse.message || t('iotCollection.loadFailed'));
            }
            if (!deviceResponse.success) {
                throw new Error(deviceResponse.message || t('iotDevice.loadFailed'));
            }
            setData(taskResponse.data || []);
            setDevices(deviceResponse.data || []);
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotCollection.loadFailed'));
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
            enabled: true,
            collectionType: 'POLLING',
            scheduleType: 'CRON',
            dataFormat: 'JSON',
            sourceType: 'DEVICE_PUSH',
        });
        setOpen(true);
    };

    const openEditModal = (record: IotCollectionTaskItem) => {
        setEditingItem(record);
        form.setFieldsValue({
            taskName: record.taskName,
            deviceId: record.deviceId,
            productKey: record.productKey,
            productName: record.productName,
            collectionType: record.collectionType,
            scheduleType: record.scheduleType,
            cronExpression: record.cronExpression,
            pollIntervalSeconds: record.pollIntervalSeconds,
            sourceType: record.sourceType,
            dataFormat: record.dataFormat,
            configJson: record.configJson,
            enabled: record.enabled,
            description: record.description,
        });
        setOpen(true);
    };

    const closeModal = () => {
        setOpen(false);
        setEditingItem(null);
        form.resetFields();
    };

    const submitTask = async () => {
        try {
            const values = await form.validateFields();
            if (values.scheduleType?.trim()?.toUpperCase() !== 'CRON' && !values.pollIntervalSeconds) {
                messageApi.error(t('iotCollection.pollIntervalRequired'));
                return;
            }
            const payload: IotCollectionTaskPayload = {
                taskName: values.taskName?.trim() || '',
                deviceId: values.deviceId,
                productKey: values.productKey?.trim() || undefined,
                productName: values.productName?.trim() || undefined,
                collectionType: values.collectionType?.trim() || '',
                scheduleType: values.scheduleType?.trim() || '',
                cronExpression: values.cronExpression?.trim() || undefined,
                pollIntervalSeconds: values.pollIntervalSeconds,
                sourceType: values.sourceType?.trim() || undefined,
                dataFormat: values.dataFormat?.trim() || undefined,
                configJson: values.configJson?.trim() || undefined,
                enabled: Boolean(values.enabled),
                description: values.description?.trim() || undefined,
            };

            setSaving(true);
            if (isEditMode && editingItem) {
                const response = await updateIotCollectionTask(editingItem.id, payload);
                if (!response.success) {
                    throw new Error(response.message || t('iotCollection.updateFailed'));
                }
                messageApi.success(t('iotCollection.updateSuccess'));
            } else {
                const response = await createIotCollectionTask(payload);
                if (!response.success) {
                    throw new Error(response.message || t('iotCollection.createFailed'));
                }
                messageApi.success(t('iotCollection.createSuccess'));
            }

            closeModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }
            messageApi.error(error instanceof Error ? error.message : t('iotCollection.saveFailed'));
        } finally {
            setSaving(false);
        }
    };

    const removeTask = async (id: number) => {
        try {
            const response = await deleteIotCollectionTask(id);
            if (!response.success) {
                throw new Error(response.message || t('iotCollection.deleteFailed'));
            }
            messageApi.success(t('iotCollection.deleteSuccess'));
            await loadData();
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : t('iotCollection.deleteFailed'));
        }
    };

    const columns: ColumnsType<IotCollectionTaskItem> = [
        { title: t('iotCollection.taskName'), dataIndex: 'taskName', key: 'taskName' },
        { title: t('iotDevice.deviceName'), dataIndex: 'deviceName', key: 'deviceName' },
        { title: t('iotCollection.productName'), dataIndex: 'productName', key: 'productName', render: (value) => value || '-' },
        { title: t('iotCollection.collectionType'), dataIndex: 'collectionType', key: 'collectionType' },
        { title: t('iotCollection.scheduleType'), dataIndex: 'scheduleType', key: 'scheduleType' },
        { title: t('iotCollection.sourceType'), dataIndex: 'sourceType', key: 'sourceType', render: (value) => value || '-' },
        { title: t('iotCollection.dataFormat'), dataIndex: 'dataFormat', key: 'dataFormat', render: (value) => value || '-' },
        { title: t('iotCollection.pollIntervalSeconds'), dataIndex: 'pollIntervalSeconds', key: 'pollIntervalSeconds', render: (value) => value || '-' },
        {
            title: t('common.enabled'),
            dataIndex: 'enabled',
            key: 'enabled',
            render: (value) => <Tag color={value ? 'green' : 'default'}>{value ? 'ENABLED' : 'DISABLED'}</Tag>,
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
                    <Button icon={<EyeOutlined />} onClick={() => navigate(buildIotCollectionDetailPath(record.id))} />
                    <Button icon={<EditOutlined />} onClick={() => openEditModal(record)} />
                    <Popconfirm title={t('iotCollection.confirmDelete')} onConfirm={() => void removeTask(record.id)}>
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
                title={t('iotCollection.pageTitle')}
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadData()}>
                            {t('common.refresh')}
                        </Button>
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                            {t('iotCollection.createButton')}
                        </Button>
                    </Space>
                }
            >
                {data.length === 0 && !loading ? (
                    <Empty description={t('iotCollection.empty')} />
                ) : (
                    <Table rowKey="id" dataSource={data} columns={columns} loading={loading} pagination={{ pageSize: 10 }} />
                )}
            </Card>

            <Modal
                title={isEditMode ? t('common.edit') : t('iotCollection.createButton')}
                open={open}
                onCancel={closeModal}
                onOk={() => void submitTask()}
                confirmLoading={saving}
                destroyOnHidden
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="taskName" label={t('iotCollection.taskName')} rules={[{ required: true, message: t('iotCollection.taskNameRequired') }]}>
                        <Input placeholder="温度采集任务" />
                    </Form.Item>
                    <Form.Item name="deviceId" label={t('iotDevice.deviceName')} rules={[{ required: true, message: t('iotCollection.deviceRequired') }]}>
                        <Select options={deviceOptions} />
                    </Form.Item>
                    <Form.Item name="productKey" label={t('iotCollection.productKey')}>
                        <Input placeholder="TEMP_SENSOR_V1" />
                    </Form.Item>
                    <Form.Item name="productName" label={t('iotCollection.productName')}>
                        <Input placeholder="温度传感器产品" />
                    </Form.Item>
                    <Form.Item name="collectionType" label={t('iotCollection.collectionType')} rules={[{ required: true, message: t('iotCollection.collectionTypeRequired') }]}>
                        <Input placeholder="POLLING" />
                    </Form.Item>
                    <Form.Item name="scheduleType" label={t('iotCollection.scheduleType')} rules={[{ required: true, message: t('iotCollection.scheduleTypeRequired') }]}>
                        <Input placeholder="CRON" />
                    </Form.Item>
                    {scheduleType?.trim()?.toUpperCase() === 'CRON' ? (
                        <Form.Item name="cronExpression" label={t('iotCollection.cronExpression')}>
                            <Input placeholder="0 */5 * * * ?" />
                        </Form.Item>
                    ) : (
                        <Form.Item name="pollIntervalSeconds" label={t('iotCollection.pollIntervalSeconds')}>
                            <Input type="number" placeholder="60" />
                        </Form.Item>
                    )}
                    <Form.Item name="sourceType" label={t('iotCollection.sourceType')}>
                        <Input placeholder="DEVICE_PUSH" />
                    </Form.Item>
                    <Form.Item name="dataFormat" label={t('iotCollection.dataFormat')}>
                        <Input placeholder="JSON" />
                    </Form.Item>
                    <Form.Item name="configJson" label={t('iotCollection.configJson')}>
                        <Input.TextArea rows={3} placeholder='{"topic":"/device/001/telemetry"}' />
                    </Form.Item>
                    <Form.Item name="enabled" label={t('common.enabled')} valuePropName="checked">
                        <Switch />
                    </Form.Item>
                    <Form.Item name="description" label={t('common.description')}>
                        <Input.TextArea rows={3} />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default IotCollectionPage;
