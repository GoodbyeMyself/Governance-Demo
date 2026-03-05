import {
    createMetadataCollectionTask,
    deleteMetadataCollectionTask,
    fetchMetadataCollectionTasks,
    updateMetadataCollectionTask,
    type MetadataCollectionScheduleType,
    type MetadataCollectionScope,
    type MetadataCollectionStrategy,
    type MetadataCollectionTaskItem,
    type MetadataCollectionTaskPayload,
} from '@/services/metadataCollection';
import {
    fetchDataSources,
    type DataSourceItem,
    type DataSourceType,
} from '@/services/dataSource';
import {
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import { useNavigate } from '@umijs/max';
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
    Tag,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

type TaskFormValues = Omit<MetadataCollectionTaskPayload, 'dataSourceId'> & {
    dataSourceId: number | null;
};

const dataSourceTypeTextMap: Record<DataSourceType, string> = {
    DATABASE: '数据库',
    FILE_SYSTEM: '文件系统',
};

const strategyTextMap: Record<MetadataCollectionStrategy, string> = {
    FULL: '全量采集',
    INCREMENTAL: '增量采集',
};

const scopeTextMap: Record<MetadataCollectionScope, string> = {
    SCHEMA: 'Schema 级',
    TABLE: '表级',
};

const scheduleTypeTextMap: Record<MetadataCollectionScheduleType, string> = {
    MANUAL: '手动触发',
    CRON: '定时 Cron',
};

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const MetadataCollectionPage: React.FC = () => {
    const navigate = useNavigate();
    const [form] = Form.useForm<TaskFormValues>();
    const [tasks, setTasks] = useState<MetadataCollectionTaskItem[]>([]);
    const [dataSources, setDataSources] = useState<DataSourceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingTask, setEditingTask] = useState<MetadataCollectionTaskItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const hasDataSource = useMemo(() => dataSources.length > 0, [dataSources.length]);
    const isEditMode = useMemo(() => Boolean(editingTask), [editingTask]);

    const loadData = async () => {
        setLoading(true);
        try {
            const [taskRes, sourceRes] = await Promise.all([
                fetchMetadataCollectionTasks(),
                fetchDataSources(),
            ]);

            if (!taskRes.success) {
                throw new Error(taskRes.message || '加载采集任务失败');
            }
            if (!sourceRes.success) {
                throw new Error(sourceRes.message || '加载数据源失败');
            }

            setTasks(taskRes.data || []);
            setDataSources(sourceRes.data || []);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载数据失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const onOpenCreate = () => {
        if (!hasDataSource) {
            messageApi.warning('请先创建至少一个数据源');
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

    const onOpenEdit = (task: MetadataCollectionTaskItem) => {
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

    const onCancelModal = () => {
        setOpen(false);
        setEditingTask(null);
        form.resetFields();
    };

    const buildPayload = (values: TaskFormValues): MetadataCollectionTaskPayload => ({
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

    const onSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (!values.dataSourceId) {
                messageApi.error('请选择数据源');
                return;
            }

            const payload = buildPayload(values);
            setSaving(true);

            if (isEditMode && editingTask) {
                const res = await updateMetadataCollectionTask(editingTask.id, payload);
                if (!res.success) {
                    throw new Error(res.message || '更新采集任务失败');
                }
                messageApi.success('采集任务更新成功');
            } else {
                const res = await createMetadataCollectionTask(payload);
                if (!res.success) {
                    throw new Error(res.message || '创建采集任务失败');
                }
                messageApi.success('采集任务创建成功');
            }

            onCancelModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }
            const msg = error instanceof Error ? error.message : '保存采集任务失败';
            messageApi.error(msg);
        } finally {
            setSaving(false);
        }
    };

    const onDelete = async (id: number) => {
        try {
            const res = await deleteMetadataCollectionTask(id);
            if (!res.success) {
                throw new Error(res.message || '删除采集任务失败');
            }
            messageApi.success('采集任务删除成功');
            await loadData();
        } catch (error) {
            const msg = error instanceof Error ? error.message : '删除采集任务失败';
            messageApi.error(msg);
        }
    };

    const columns: ColumnsType<MetadataCollectionTaskItem> = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 80,
        },
        {
            title: '任务名称',
            dataIndex: 'taskName',
            width: 180,
            ellipsis: true,
        },
        {
            title: '数据源',
            dataIndex: 'dataSourceName',
            width: 200,
            ellipsis: true,
        },
        {
            title: '数据源类型',
            dataIndex: 'dataSourceType',
            width: 120,
            render: (value: DataSourceType) => (
                <Tag color={value === 'DATABASE' ? 'blue' : 'green'}>
                    {dataSourceTypeTextMap[value]}
                </Tag>
            ),
        },
        {
            title: '采集策略',
            dataIndex: 'strategy',
            width: 120,
            render: (value: MetadataCollectionStrategy) => strategyTextMap[value],
        },
        {
            title: '采集范围',
            dataIndex: 'scope',
            width: 120,
            render: (value: MetadataCollectionScope) => scopeTextMap[value],
        },
        {
            title: '目标匹配规则',
            dataIndex: 'targetPattern',
            width: 220,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: '调度方式',
            dataIndex: 'scheduleType',
            width: 140,
            render: (value: MetadataCollectionScheduleType) => scheduleTypeTextMap[value],
        },
        {
            title: 'Cron 表达式',
            dataIndex: 'cronExpression',
            width: 180,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: '启用状态',
            dataIndex: 'enabled',
            width: 100,
            render: (value: boolean) =>
                value ? <Tag color="success">启用</Tag> : <Tag color="default">停用</Tag>,
        },
        {
            title: '描述',
            dataIndex: 'description',
            width: 200,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            width: 180,
            render: formatTime,
        },
        {
            title: '更新时间',
            dataIndex: 'updatedAt',
            width: 180,
            render: formatTime,
        },
        {
            title: '操作',
            key: 'action',
            width: 220,
            fixed: 'right',
            render: (_, record) => (
                <Space size="middle">
                    <Button
                        type="link"
                        size="small"
                        icon={<EyeOutlined />}
                        onClick={() => navigate(`/metadata-collection/${record.id}`)}
                    >
                        详情
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => onOpenEdit(record)}
                    >
                        编辑
                    </Button>
                    <Popconfirm
                        title="确认删除该采集任务吗？"
                        okText="确认"
                        cancelText="取消"
                        onConfirm={() => onDelete(record.id)}
                    >
                        <Button
                            type="link"
                            danger
                            size="small"
                            icon={<DeleteOutlined />}
                        >
                            删除
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div className={styles.page}>
            {contextHolder}
            <Card
                title="元数据采集任务"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadData()}>
                            刷新
                        </Button>
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={onOpenCreate}
                            disabled={!hasDataSource}
                        >
                            新增采集任务
                        </Button>
                    </Space>
                }
            >
                <Table<MetadataCollectionTaskItem>
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={tasks}
                    scroll={{ x: 2360 }}
                    pagination={{
                        showSizeChanger: true,
                        defaultPageSize: 10,
                        showTotal: (total) => `共 ${total} 条`,
                    }}
                />
            </Card>

            <Modal
                title={isEditMode ? '编辑采集任务' : '新增采集任务'}
                open={open}
                confirmLoading={saving}
                onCancel={onCancelModal}
                onOk={() => void onSubmit()}
                destroyOnClose
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
                        label="任务名称"
                        name="taskName"
                        rules={[
                            { required: true, message: '请输入任务名称' },
                            { max: 100, message: '任务名称不能超过 100 个字符' },
                        ]}
                    >
                        <Input placeholder="例如：mysql-prod-schema-collector" />
                    </Form.Item>

                    <Form.Item
                        label="数据源"
                        name="dataSourceId"
                        rules={[{ required: true, message: '请选择数据源' }]}
                    >
                        <Select
                            showSearch
                            optionFilterProp="label"
                            placeholder="请选择已存在的数据源"
                            options={dataSources.map((item) => ({
                                value: item.id,
                                label: `${item.name}（${dataSourceTypeTextMap[item.type]}）`,
                            }))}
                        />
                    </Form.Item>

                    <Form.Item
                        label="采集策略"
                        name="strategy"
                        rules={[{ required: true, message: '请选择采集策略' }]}
                    >
                        <Select
                            options={[
                                { label: '全量采集', value: 'FULL' },
                                { label: '增量采集', value: 'INCREMENTAL' },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item
                        label="采集范围"
                        name="scope"
                        rules={[{ required: true, message: '请选择采集范围' }]}
                    >
                        <Select
                            options={[
                                { label: 'Schema 级', value: 'SCHEMA' },
                                { label: '表级', value: 'TABLE' },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item
                        label="目标匹配规则"
                        name="targetPattern"
                        rules={[{ max: 500, message: '目标匹配规则不能超过 500 个字符' }]}
                    >
                        <Input placeholder="例如：sales_* 或 analytics.orders_*" />
                    </Form.Item>

                    <Form.Item
                        label="调度方式"
                        name="scheduleType"
                        rules={[{ required: true, message: '请选择调度方式' }]}
                    >
                        <Select
                            options={[
                                { label: '手动触发', value: 'MANUAL' },
                                { label: '定时 Cron', value: 'CRON' },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item
                        noStyle
                        shouldUpdate={(prev, next) =>
                            prev.scheduleType !== next.scheduleType
                        }
                    >
                        {({ getFieldValue }) =>
                            getFieldValue('scheduleType') === 'CRON' ? (
                                <Form.Item
                                    label="Cron 表达式"
                                    name="cronExpression"
                                    rules={[
                                        { required: true, message: '请输入 Cron 表达式' },
                                        {
                                            max: 100,
                                            message: 'Cron 表达式不能超过 100 个字符',
                                        },
                                    ]}
                                >
                                    <Input placeholder="例如：0 0/30 * * * ?" />
                                </Form.Item>
                            ) : null
                        }
                    </Form.Item>

                    <Form.Item
                        label="采集配置（JSON）"
                        name="configJson"
                        rules={[{ max: 2000, message: '采集配置不能超过 2000 个字符' }]}
                    >
                        <Input.TextArea
                            rows={4}
                            placeholder='例如：{"includeViews":true,"maxTables":500}'
                        />
                    </Form.Item>

                    <Form.Item
                        label="描述"
                        name="description"
                        rules={[{ max: 500, message: '描述不能超过 500 个字符' }]}
                    >
                        <Input.TextArea rows={3} />
                    </Form.Item>

                    <Form.Item
                        label="是否启用"
                        name="enabled"
                        valuePropName="checked"
                        rules={[{ required: true, message: '请设置启用状态' }]}
                    >
                        <Switch checkedChildren="启用" unCheckedChildren="停用" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default MetadataCollectionPage;
