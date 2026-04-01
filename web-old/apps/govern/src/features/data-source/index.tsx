import {
    DeleteOutlined,
    EditOutlined,
    PlusOutlined,
    ReloadOutlined,
} from '@ant-design/icons';
import {
    createDataSource,
    deleteDataSource,
    fetchDataSources,
    updateDataSource,
    type DataSourceItem,
    type DataSourcePayload,
} from '@governance/api';
import { DataSourceTypeTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    formatDateTime,
    getDataSourceTypeOptions,
} from '@governance/utils';
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
    message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.module.less';

const DataSourcePage: React.FC = () => {
    const { t } = useI18n();
    const [form] = Form.useForm<DataSourcePayload>();
    const [data, setData] = useState<DataSourceItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingItem, setEditingItem] = useState<DataSourceItem | null>(null);
    const [messageApi, contextHolder] = message.useMessage();
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(9);

    const isEditMode = Boolean(editingItem);
    const dataSourceTypeOptions = useMemo(() => getDataSourceTypeOptions(), [t]);

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
            const response = await fetchDataSources();
            if (!response.success) {
                throw new Error(response.message || t('dataSource.loadFailed'));
            }
            setData(response.data || []);
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('dataSource.loadFailed');
            messageApi.error(messageText);
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
        form.setFieldValue('type', 'DATABASE');
        setOpen(true);
    };

    const openEditModal = (record: DataSourceItem) => {
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

    const closeModal = () => {
        setOpen(false);
        setEditingItem(null);
        form.resetFields();
    };

    const submitDataSource = async () => {
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
            if (isEditMode && editingItem) {
                const response = await updateDataSource(editingItem.id, payload);
                if (!response.success) {
                    throw new Error(
                        response.message || t('dataSource.updateFailed'),
                    );
                }
                messageApi.success(t('dataSource.updateSuccess'));
            } else {
                const response = await createDataSource(payload);
                if (!response.success) {
                    throw new Error(
                        response.message || t('dataSource.createFailed'),
                    );
                }
                messageApi.success(t('dataSource.createSuccess'));
            }

            closeModal();
            await loadData();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error
                    ? error.message
                    : t('dataSource.saveFailed');
            messageApi.error(messageText);
        } finally {
            setSaving(false);
        }
    };

    const removeDataSource = async (id: number) => {
        try {
            const response = await deleteDataSource(id);
            if (!response.success) {
                throw new Error(response.message || t('dataSource.deleteFailed'));
            }
            messageApi.success(t('dataSource.deleteSuccess'));
            await loadData();
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('dataSource.deleteFailed');
            messageApi.error(messageText);
        }
    };

    return (
        <div className={styles.page}>
            {contextHolder}

            <Card
                title={t('dataSource.pageTitle')}
                loading={loading}
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
                        >
                            {t('dataSource.createButton')}
                        </Button>
                    </Space>
                }
            >
                {!loading && data.length === 0 ? (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description={t('dataSource.empty')}
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
                                                        <span
                                                            className={
                                                                styles.cardTitle
                                                            }
                                                        >
                                                            {item.name || '-'}
                                                        </span>
                                                        <DataSourceTypeTag
                                                            type={item.type}
                                                        />
                                                    </Space>
                                                }
                                            >
                                                <div
                                                    className={
                                                        styles.cardContent
                                                    }
                                                >
                                                    <div
                                                        className={
                                                            styles.infoRow
                                                        }
                                                    >
                                                        <span
                                                            className={
                                                                styles.infoLabel
                                                            }
                                                        >
                                                            {t(
                                                                'dataSource.connectionUrl',
                                                            )}
                                                        </span>
                                                        <span
                                                            className={
                                                                styles.infoValue
                                                            }
                                                        >
                                                            {item.connectionUrl ||
                                                                '-'}
                                                        </span>
                                                    </div>
                                                    <div
                                                        className={
                                                            styles.infoRow
                                                        }
                                                    >
                                                        <span
                                                            className={
                                                                styles.infoLabel
                                                            }
                                                        >
                                                            {t('common.username')}
                                                        </span>
                                                        <span
                                                            className={
                                                                styles.infoValue
                                                            }
                                                        >
                                                            {item.username ||
                                                                '-'}
                                                        </span>
                                                    </div>
                                                    <div
                                                        className={
                                                            styles.infoRow
                                                        }
                                                    >
                                                        <span
                                                            className={
                                                                styles.infoLabel
                                                            }
                                                        >
                                                            {t(
                                                                'common.description',
                                                            )}
                                                        </span>
                                                        <span
                                                            className={
                                                                styles.infoValue
                                                            }
                                                        >
                                                            {item.description ||
                                                                '-'}
                                                        </span>
                                                    </div>
                                                    <div
                                                        className={
                                                            styles.infoRow
                                                        }
                                                    >
                                                        <span
                                                            className={
                                                                styles.infoLabel
                                                            }
                                                        >
                                                            {t('common.updatedAt')}
                                                        </span>
                                                        <span
                                                            className={
                                                                styles.infoValue
                                                            }
                                                        >
                                                            {formatDateTime(
                                                                item.updatedAt,
                                                            )}
                                                        </span>
                                                    </div>
                                                </div>

                                                <div
                                                    className={
                                                        styles.cardActions
                                                    }
                                                >
                                                    <Button
                                                        type="link"
                                                        size="small"
                                                        icon={<EditOutlined />}
                                                        onClick={() =>
                                                            openEditModal(item)
                                                        }
                                                    >
                                                        {t('common.edit')}
                                                    </Button>
                                                    <Popconfirm
                                                        title={t(
                                                            'dataSource.confirmDelete',
                                                        )}
                                                        okText={t(
                                                            'common.confirm',
                                                        )}
                                                        cancelText={t(
                                                            'common.cancel',
                                                        )}
                                                        onConfirm={() =>
                                                            void removeDataSource(
                                                                item.id,
                                                            )
                                                        }
                                                    >
                                                        <Button
                                                            type="link"
                                                            danger
                                                            size="small"
                                                            icon={
                                                                <DeleteOutlined />
                                                            }
                                                        >
                                                            {t('common.delete')}
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
                                    showTotal={(total) =>
                                        t('common.totalRows', { total })
                                    }
                                />
                            </div>
                        </>
                    )
                )}
            </Card>

            <Modal
                title={t(
                    isEditMode
                        ? 'dataSource.editTitle'
                        : 'dataSource.createTitle',
                )}
                open={open}
                confirmLoading={saving}
                onCancel={closeModal}
                onOk={() => void submitDataSource()}
                destroyOnHidden
                maskClosable={false}
            >
                <Form
                    form={form}
                    layout="vertical"
                    initialValues={{ type: 'DATABASE' }}
                >
                    <Form.Item
                        label={t('common.name')}
                        name="name"
                        rules={[
                            {
                                required: true,
                                message: t('dataSource.nameRequired'),
                            },
                            {
                                max: 100,
                                message: t('dataSource.nameMax'),
                            },
                        ]}
                    >
                        <Input placeholder={t('dataSource.namePlaceholder')} />
                    </Form.Item>

                    <Form.Item
                        label={t('common.type')}
                        name="type"
                        rules={[
                            {
                                required: true,
                                message: t('dataSource.typeRequired'),
                            },
                        ]}
                    >
                        <Select options={dataSourceTypeOptions} />
                    </Form.Item>

                    <Form.Item
                        label={t('dataSource.connectionUrl')}
                        name="connectionUrl"
                        rules={[
                            {
                                max: 500,
                                message: t('dataSource.connectionUrlMax'),
                            },
                        ]}
                    >
                        <Input
                            placeholder={t(
                                'dataSource.connectionUrlPlaceholder',
                            )}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('common.username')}
                        name="username"
                        rules={[
                            {
                                max: 100,
                                message: t('dataSource.usernameMax'),
                            },
                        ]}
                    >
                        <Input
                            placeholder={t('dataSource.usernamePlaceholder')}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t(
                            isEditMode
                                ? 'dataSource.passwordEditHint'
                                : 'common.password',
                        )}
                        name="password"
                        rules={[
                            {
                                max: 100,
                                message: t('dataSource.passwordMax'),
                            },
                        ]}
                    >
                        <Input.Password
                            placeholder={t('dataSource.passwordPlaceholder')}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('common.description')}
                        name="description"
                        rules={[
                            {
                                max: 500,
                                message: t('dataSource.descriptionMax'),
                            },
                        ]}
                    >
                        <Input.TextArea
                            rows={3}
                            placeholder={t(
                                'dataSource.descriptionPlaceholder',
                            )}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default DataSourcePage;
