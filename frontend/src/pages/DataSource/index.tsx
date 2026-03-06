import {
    createDataSource,
    deleteDataSource,
    fetchDataSources,
    type DataSourceItem,
    type DataSourcePayload,
    type DataSourceType,
    updateDataSource,
} from '@/services/dataSource';
import {
    DeleteOutlined,
    EditOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import {
    Button,
    Card,
    Col,
    Empty,
    Form,
    Input,
    Modal,
    Pagination,
    Popconfirm,
    Row,
    Select,
    Space,
    Tag,
    message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.module.less';

const typeTextMap: Record<DataSourceType, string> = {
    DATABASE: '数据库',
    FILE_SYSTEM: '文件系统',
};

const typeColorMap: Record<DataSourceType, string> = {
    DATABASE: 'blue',
    FILE_SYSTEM: 'green',
};

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const DataSourcePage: React.FC = () => {
    const [form] = Form.useForm<DataSourcePayload>();
    const [data, setData] = useState<DataSourceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingItem, setEditingItem] = useState<DataSourceItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(9);

    const isEdit = useMemo(() => Boolean(editingItem), [editingItem]);

    const pagedData = useMemo(() => {
        const startIndex = (currentPage - 1) * pageSize;
        return data.slice(startIndex, startIndex + pageSize);
    }, [currentPage, data, pageSize]);

    useEffect(() => {
        const maxPage = Math.max(1, Math.ceil(data.length / pageSize));
        if (currentPage > maxPage) {
            setCurrentPage(maxPage);
        }
    }, [currentPage, data.length, pageSize]);

    const loadData = async () => {
        setLoading(true);
        try {
            const res = await fetchDataSources();
            if (!res.success) {
                throw new Error(res.message || '加载数据源失败');
            }
            setData(res.data || []);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载数据源失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const onOpenCreate = () => {
        setEditingItem(null);
        form.resetFields();
        form.setFieldValue('type', 'DATABASE');
        setOpen(true);
    };

    const onOpenEdit = (record: DataSourceItem) => {
        setEditingItem(record);
        form.setFieldsValue({
            name: record.name,
            type: record.type,
            connectionUrl: record.connectionUrl,
            username: record.username,
            description: record.description,
            password: '',
        });
        setOpen(true);
    };

    const onCancelModal = () => {
        setOpen(false);
        setEditingItem(null);
        form.resetFields();
    };

    const onSubmit = async () => {
        try {
            const values = await form.validateFields();
            const payload: DataSourcePayload = {
                name: values.name?.trim() || '',
                type: values.type,
                connectionUrl: values.connectionUrl?.trim() || undefined,
                username: values.username?.trim() || undefined,
                password: values.password?.trim() || undefined,
                description: values.description?.trim() || undefined,
            };

            setSaving(true);
            if (isEdit && editingItem) {
                const res = await updateDataSource(editingItem.id, payload);
                if (!res.success) {
                    throw new Error(res.message || '更新数据源失败');
                }
                messageApi.success('数据源更新成功');
            } else {
                const res = await createDataSource(payload);
                if (!res.success) {
                    throw new Error(res.message || '新增数据源失败');
                }
                messageApi.success('数据源创建成功');
            }

            onCancelModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }
            const msg = error instanceof Error ? error.message : '保存数据源失败';
            messageApi.error(msg);
        } finally {
            setSaving(false);
        }
    };

    const onDelete = async (id: number) => {
        try {
            const res = await deleteDataSource(id);
            if (!res.success) {
                throw new Error(res.message || '删除数据源失败');
            }
            messageApi.success('删除成功');
            await loadData();
        } catch (error) {
            const msg = error instanceof Error ? error.message : '删除数据源失败';
            messageApi.error(msg);
        }
    };

    return (
        <div className={styles.page}>
            {contextHolder}
            <Card
                title="数据源管理"
                loading={loading}
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadData()}>
                            刷新
                        </Button>
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={onOpenCreate}
                        >
                            新增数据源
                        </Button>
                    </Space>
                }
            >
                {!loading && data.length === 0 ? (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description="暂无数据源，请先新增"
                    />
                ) : (
                    !loading && (
                        <>
                            <div className={styles.cardList}>
                                <Row gutter={[16, 16]}>
                                    {pagedData.map((item) => (
                                        <Col
                                            key={item.id}
                                            xs={24}
                                            sm={24}
                                            md={12}
                                            lg={12}
                                            xl={8}
                                            xxl={6}
                                        >
                                            <Card
                                                hoverable
                                                className={styles.sourceCard}
                                                title={
                                                    <Space size={8} wrap={false}>
                                                        <span className={styles.cardTitle}>
                                                            {item.name || '-'}
                                                        </span>
                                                        <Tag color={typeColorMap[item.type]}>
                                                            {typeTextMap[item.type]}
                                                        </Tag>
                                                    </Space>
                                                }
                                            >
                                                <div className={styles.cardContent}>
                                                    <div className={styles.infoRow}>
                                                        <span className={styles.infoLabel}>
                                                            连接地址
                                                        </span>
                                                        <span className={styles.infoValue}>
                                                            {item.connectionUrl || '-'}
                                                        </span>
                                                    </div>
                                                    <div className={styles.infoRow}>
                                                        <span className={styles.infoLabel}>描述</span>
                                                        <span className={styles.infoValue}>
                                                            {item.description || '-'}
                                                        </span>
                                                    </div>
                                                    <div className={styles.infoRow}>
                                                        <span className={styles.infoLabel}>
                                                            创建时间
                                                        </span>
                                                        <span className={styles.infoValue}>
                                                            {formatTime(item.createdAt)}
                                                        </span>
                                                    </div>
                                                </div>
                                                <div className={styles.cardActions}>
                                                    <Button
                                                        type="link"
                                                        size="small"
                                                        icon={<EditOutlined />}
                                                        onClick={() => onOpenEdit(item)}
                                                    >
                                                        编辑
                                                    </Button>
                                                    <Popconfirm
                                                        title="确认删除该数据源吗？"
                                                        okText="确认"
                                                        cancelText="取消"
                                                        onConfirm={() => onDelete(item.id)}
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
                                                </div>
                                            </Card>
                                        </Col>
                                    ))}
                                </Row>
                            </div>

                            <div
                                style={{
                                    marginTop: 16,
                                    display: 'flex',
                                    justifyContent: 'flex-end',
                                }}
                            >
                                <Pagination
                                    current={currentPage}
                                    pageSize={pageSize}
                                    total={data.length}
                                    showSizeChanger
                                    pageSizeOptions={[9, 18, 36, 72]}
                                    onChange={(page, size) => {
                                        setCurrentPage(page);
                                        setPageSize(size);
                                    }}
                                    showTotal={(total) => `共 ${total} 条`}
                                />
                            </div>
                        </>
                    )
                )}
            </Card>

            <Modal
                title={isEdit ? '编辑数据源' : '新增数据源'}
                open={open}
                confirmLoading={saving}
                onCancel={onCancelModal}
                onOk={() => void onSubmit()}
                mask={{ closable: false }}
            >
                <Form form={form} layout="vertical" initialValues={{ type: 'DATABASE' }}>
                    <Form.Item
                        label="数据源名称"
                        name="name"
                        rules={[
                            { required: true, message: '请输入数据源名称' },
                            { max: 100, message: '名称不能超过 100 个字符' },
                        ]}
                    >
                        <Input placeholder="例如：MySQL-Prod" />
                    </Form.Item>

                    <Form.Item
                        label="类型"
                        name="type"
                        rules={[{ required: true, message: '请选择类型' }]}
                    >
                        <Select
                            options={[
                                { label: '数据库', value: 'DATABASE' },
                                { label: '文件系统', value: 'FILE_SYSTEM' },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item
                        label="连接地址"
                        name="connectionUrl"
                        rules={[
                            {
                                max: 500,
                                message: '连接地址不能超过 500 个字符',
                            },
                        ]}
                    >
                        <Input placeholder="例如：jdbc:mysql://localhost:3306/demo" />
                    </Form.Item>

                    <Form.Item
                        label="用户名"
                        name="username"
                        rules={[
                            { max: 100, message: '用户名不能超过 100 个字符' },
                        ]}
                    >
                        <Input placeholder="例如：root" />
                    </Form.Item>

                    <Form.Item
                        label={isEdit ? '密码（留空表示不更新）' : '密码'}
                        name="password"
                        rules={[
                            { max: 100, message: '密码不能超过 100 个字符' },
                        ]}
                    >
                        <Input.Password placeholder="请输入密码" />
                    </Form.Item>

                    <Form.Item
                        label="描述"
                        name="description"
                        rules={[
                            { max: 500, message: '描述不能超过 500 个字符' },
                        ]}
                    >
                        <Input.TextArea rows={3} placeholder="请输入描述信息" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default DataSourcePage;
