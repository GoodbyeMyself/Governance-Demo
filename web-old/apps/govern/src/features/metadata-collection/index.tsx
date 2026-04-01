import {
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import {
    createMetadataCollectionTask,
    deleteMetadataCollectionTask,
    fetchDataSources,
    fetchMetadataCollectionTasks,
    updateMetadataCollectionTask,
    type DataSourceItem,
    type MetadataCollectionTaskItem,
    type MetadataCollectionTaskPayload,
} from '@governance/api';
import { DataSourceTypeTag, EnabledTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    buildMetadataCollectionDetailPath,
    formatDateTime,
    getDataSourceTypeText,
    getMetadataScheduleTypeOptions,
    getMetadataScheduleTypeText,
    getMetadataScopeOptions,
    getMetadataScopeText,
    getMetadataStrategyOptions,
    getMetadataStrategyText,
} from '@governance/utils';
import {
    Button,
    Card,
    Form,
    Input,
    Modal,
    Popconfirm,
    Select,
    Space,
    Switch,
    Table,
    Tooltip,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

type TaskFormValues = Omit<MetadataCollectionTaskPayload, 'dataSourceId'> & {
    dataSourceId: number | null;
};

const MetadataCollectionPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [form] = Form.useForm<TaskFormValues>();
    const scheduleType = Form.useWatch('scheduleType', form);
    const [tasks, setTasks] = useState<MetadataCollectionTaskItem[]>([]);
    const [dataSources, setDataSources] = useState<DataSourceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingTask, setEditingTask] =
        useState<MetadataCollectionTaskItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const hasDataSource = dataSources.length > 0;
    const isEditMode = Boolean(editingTask);

    const dataSourceOptions = useMemo(
        () =>
            dataSources.map((item) => ({
                value: item.id,
                label: `${item.name}（${getDataSourceTypeText(item.type)}）`,
            })),
        [dataSources, t],
    );

    const strategyOptions = useMemo(() => getMetadataStrategyOptions(), [t]);
    const scopeOptions = useMemo(() => getMetadataScopeOptions(), [t]);
    const scheduleTypeOptions = useMemo(
        () => getMetadataScheduleTypeOptions(),
        [t],
    );

    const loadData = async () => {
        setLoading(true);
        try {
            const [taskResponse, sourceResponse] = await Promise.all([
                fetchMetadataCollectionTasks(),
                fetchDataSources(),
            ]);

            if (!taskResponse.success) {
                throw new Error(taskResponse.message || t('metadata.loadFailed'));
            }
            if (!sourceResponse.success) {
                throw new Error(sourceResponse.message || t('dataSource.loadFailed'));
            }

            setTasks(taskResponse.data || []);
            setDataSources(sourceResponse.data || []);
        } catch (error) {
            const messageText =
                error instanceof Error ? error.message : t('metadata.loadFailed');
            messageApi.error(messageText);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const openCreateModal = () => {
        if (!hasDataSource) {
            messageApi.warning(t('metadata.requireDataSourceFirst'));
            return;
        }

        setEditingTask(null);
        form.resetFields();
        form.setFieldsValue({
            strategy: 'FULL',
            scope: 'SCHEMA',
            scheduleType: 'MANUAL',
            enabled: true,
            dataSourceId: dataSources[0]?.id || null,
            cronExpression: undefined,
        });
        setOpen(true);
    };

    const openEditModal = (task: MetadataCollectionTaskItem) => {
        setEditingTask(task);
        form.resetFields();
        form.setFieldsValue({
            taskName: task.taskName,
            dataSourceId: task.dataSourceId,
            strategy: task.strategy,
            scope: task.scope,
            targetPattern: task.targetPattern,
            scheduleType: task.scheduleType,
            cronExpression: task.cronExpression,
            configJson: task.configJson,
            description: task.description,
            enabled: task.enabled,
        });
        setOpen(true);
    };

    const closeModal = () => {
        setOpen(false);
        setEditingTask(null);
        form.resetFields();
    };

    const buildPayload = (
        values: TaskFormValues,
    ): MetadataCollectionTaskPayload => ({
        taskName: values.taskName.trim(),
        dataSourceId: values.dataSourceId || 0,
        strategy: values.strategy,
        scope: values.scope,
        targetPattern: values.targetPattern?.trim() || undefined,
        scheduleType: values.scheduleType,
        cronExpression:
            values.scheduleType === 'CRON'
                ? values.cronExpression?.trim() || undefined
                : undefined,
        configJson: values.configJson?.trim() || undefined,
        description: values.description?.trim() || undefined,
        enabled: values.enabled,
    });

    const submitTask = async () => {
        try {
            const values = await form.validateFields();
            if (!values.dataSourceId) {
                messageApi.error(t('metadata.dataSourceRequired'));
                return;
            }

            const payload = buildPayload(values);
            setSaving(true);

            if (isEditMode && editingTask) {
                const response = await updateMetadataCollectionTask(
                    editingTask.id,
                    payload,
                );
                if (!response.success) {
                    throw new Error(
                        response.message || t('metadata.updateFailed'),
                    );
                }
                messageApi.success(t('metadata.updateSuccess'));
            } else {
                const response = await createMetadataCollectionTask(payload);
                if (!response.success) {
                    throw new Error(
                        response.message || t('metadata.createFailed'),
                    );
                }
                messageApi.success(t('metadata.createSuccess'));
            }

            closeModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error ? error.message : t('metadata.saveFailed');
            messageApi.error(messageText);
        } finally {
            setSaving(false);
        }
    };

    const removeTask = async (id: number) => {
        try {
            const response = await deleteMetadataCollectionTask(id);
            if (!response.success) {
                throw new Error(response.message || t('metadata.deleteFailed'));
            }
            messageApi.success(t('metadata.deleteSuccess'));
            await loadData();
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('metadata.deleteFailed');
            messageApi.error(messageText);
        }
    };

    const columns: ColumnsType<MetadataCollectionTaskItem> = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 70,
        },
        {
            title: t('metadata.field.taskName'),
            dataIndex: 'taskName',
            width: 180,
            ellipsis: true,
        },
        {
            title: t('metadata.field.dataSource'),
            dataIndex: 'dataSourceName',
            width: 180,
            ellipsis: true,
        },
        {
            title: t('metadata.field.dataSourceType'),
            dataIndex: 'dataSourceType',
            width: 120,
            render: (value) => <DataSourceTypeTag type={value} />,
        },
        {
            title: t('metadata.field.strategy'),
            dataIndex: 'strategy',
            width: 120,
            render: getMetadataStrategyText,
        },
        {
            title: t('metadata.field.scope'),
            dataIndex: 'scope',
            width: 120,
            render: getMetadataScopeText,
        },
        {
            title: t('metadata.field.scheduleType'),
            dataIndex: 'scheduleType',
            width: 120,
            render: getMetadataScheduleTypeText,
        },
        {
            title: t('common.status'),
            dataIndex: 'enabled',
            width: 100,
            render: (value: boolean) => <EnabledTag enabled={value} />,
        },
        {
            title: t('common.description'),
            dataIndex: 'description',
            width: 220,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: t('common.createdAt'),
            dataIndex: 'createdAt',
            width: 180,
            render: formatDateTime,
        },
        {
            title: t('common.actions'),
            key: 'action',
            fixed: 'right',
            width: 140,
            render: (_, record) => (
                <Space size={4}>
                    <Tooltip title={t('common.details')}>
                        <Button
                            type="text"
                            size="small"
                            icon={<EyeOutlined />}
                            onClick={() =>
                                navigate(
                                    buildMetadataCollectionDetailPath(record.id),
                                )
                            }
                        />
                    </Tooltip>
                    <Tooltip title={t('common.edit')}>
                        <Button
                            type="text"
                            size="small"
                            icon={<EditOutlined />}
                            onClick={() => openEditModal(record)}
                        />
                    </Tooltip>
                    <Popconfirm
                        title={t('metadata.confirmDelete')}
                        okText={t('common.confirm')}
                        cancelText={t('common.cancel')}
                        onConfirm={() => void removeTask(record.id)}
                    >
                        <Tooltip title={t('common.delete')}>
                            <Button
                                type="text"
                                danger
                                size="small"
                                icon={<DeleteOutlined />}
                            />
                        </Tooltip>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div className={styles.page}>
            {contextHolder}

            <Card
                title={t('metadata.pageTitle')}
                extra={
                    <Space>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() => void loadData()}
                        >
                            {t('common.refresh')}
                        </Button>
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={openCreateModal}
                            disabled={!hasDataSource}
                        >
                            {t('metadata.createButton')}
                        </Button>
                    </Space>
                }
            >
                <Table<MetadataCollectionTaskItem>
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={tasks}
                    scroll={{ x: 1500 }}
                    pagination={{
                        showSizeChanger: true,
                        defaultPageSize: 10,
                        showTotal: (total) => t('common.totalRows', { total }),
                    }}
                />
            </Card>

            <Modal
                title={t(
                    isEditMode ? 'metadata.editTitle' : 'metadata.createTitle',
                )}
                open={open}
                confirmLoading={saving}
                onCancel={closeModal}
                onOk={() => void submitTask()}
                destroyOnHidden
                maskClosable={false}
            >
                <Form<TaskFormValues>
                    form={form}
                    layout="vertical"
                    initialValues={{
                        strategy: 'FULL',
                        scope: 'SCHEMA',
                        scheduleType: 'MANUAL',
                        enabled: true,
                        dataSourceId: null,
                    }}
                >
                    <Form.Item
                        label={t('metadata.field.taskName')}
                        name="taskName"
                        rules={[
                            {
                                required: true,
                                message: t('metadata.taskNameRequired'),
                            },
                            {
                                max: 100,
                                message: t('metadata.taskNameMax'),
                            },
                        ]}
                    >
                        <Input placeholder={t('metadata.taskNamePlaceholder')} />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.dataSource')}
                        name="dataSourceId"
                        rules={[
                            {
                                required: true,
                                message: t('metadata.dataSourceRequired'),
                            },
                        ]}
                    >
                        <Select
                            showSearch
                            optionFilterProp="label"
                            placeholder={t('metadata.dataSourcePlaceholder')}
                            options={dataSourceOptions}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.strategy')}
                        name="strategy"
                        rules={[
                            {
                                required: true,
                                message: t('metadata.strategyRequired'),
                            },
                        ]}
                    >
                        <Select options={strategyOptions} />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.scope')}
                        name="scope"
                        rules={[
                            {
                                required: true,
                                message: t('metadata.scopeRequired'),
                            },
                        ]}
                    >
                        <Select options={scopeOptions} />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.targetPattern')}
                        name="targetPattern"
                        rules={[
                            {
                                max: 500,
                                message: t('metadata.targetPatternMax'),
                            },
                        ]}
                    >
                        <Input
                            placeholder={t(
                                'metadata.targetPatternPlaceholder',
                            )}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.scheduleType')}
                        name="scheduleType"
                        rules={[
                            {
                                required: true,
                                message: t('metadata.scheduleTypeRequired'),
                            },
                        ]}
                    >
                        <Select options={scheduleTypeOptions} />
                    </Form.Item>

                    {scheduleType === 'CRON' ? (
                        <Form.Item
                            label={t('metadata.field.cronExpression')}
                            name="cronExpression"
                            rules={[
                                {
                                    required: true,
                                    message: t('metadata.cronRequired'),
                                },
                                {
                                    max: 100,
                                    message: t('metadata.cronMax'),
                                },
                            ]}
                        >
                            <Input placeholder={t('metadata.cronPlaceholder')} />
                        </Form.Item>
                    ) : null}

                    <Form.Item
                        label={t('metadata.field.configJson')}
                        name="configJson"
                        rules={[
                            {
                                max: 2000,
                                message: t('metadata.configJsonMax'),
                            },
                        ]}
                    >
                        <Input.TextArea
                            rows={4}
                            placeholder={t('metadata.configJsonPlaceholder')}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('common.description')}
                        name="description"
                        rules={[
                            {
                                max: 500,
                                message: t('metadata.descriptionMax'),
                            },
                        ]}
                    >
                        <Input.TextArea rows={3} />
                    </Form.Item>

                    <Form.Item
                        label={t('metadata.field.enabled')}
                        name="enabled"
                        valuePropName="checked"
                    >
                        <Switch
                            checkedChildren={t('common.enabled')}
                            unCheckedChildren={t('common.disabled')}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default MetadataCollectionPage;
