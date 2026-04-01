import { EditOutlined, EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchBmsRoleDefinitions,
    updateBmsRoleDefinition,
    type BmsRoleDefinition,
} from '@governance/api';
import { useI18n } from '@governance/i18n';
import { getUserRoleText } from '@governance/utils';
import { Button, Card, Form, Input, Modal, Space, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import styles from './index.module.less';

const RoleManagementPage: React.FC = () => {
    const { t } = useI18n();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [roles, setRoles] = useState<BmsRoleDefinition[]>([]);
    const [editingRole, setEditingRole] = useState<BmsRoleDefinition | null>(
        null,
    );
    const [modalOpen, setModalOpen] = useState(false);
    const [form] = Form.useForm<{ roleName: string }>();
    const [messageApi, contextHolder] = message.useMessage();

    const loadRoles = async () => {
        setLoading(true);
        try {
            const response = await fetchBmsRoleDefinitions();
            if (!response.success || !response.data) {
                throw new Error(
                    response.message || t('roleManagement.loadRolesFailed'),
                );
            }
            setRoles(response.data);
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('roleManagement.loadRolesFailed');
            messageApi.error(messageText);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadRoles();
    }, []);

    const openRoleModal = (role: BmsRoleDefinition) => {
        setEditingRole(role);
        setModalOpen(true);
        form.setFieldsValue({
            roleName: role.roleName,
        });
    };

    const closeRoleModal = () => {
        setModalOpen(false);
        setEditingRole(null);
        form.resetFields();
    };

    const submitRoleUpdate = async () => {
        if (!editingRole) {
            return;
        }

        if (!editingRole.editable) {
            closeRoleModal();
            return;
        }

        try {
            const values = await form.validateFields();
            setSaving(true);
            const response = await updateBmsRoleDefinition(
                editingRole.roleCode,
                {
                    roleName: values.roleName.trim(),
                },
            );
            if (!response.success) {
                throw new Error(
                    response.message || t('roleManagement.updateRoleFailed'),
                );
            }

            messageApi.success(t('roleManagement.updateRoleSuccess'));
            closeRoleModal();
            await loadRoles();
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error
                    ? error.message
                    : t('roleManagement.updateRoleFailed');
            messageApi.error(messageText);
        } finally {
            setSaving(false);
        }
    };

    const columns: ColumnsType<BmsRoleDefinition> = [
        {
            title: t('common.role'),
            dataIndex: 'roleCode',
            width: 180,
            render: (value: string) => (
                <Space size={8}>
                    <Tag color={value === 'ADMIN' ? 'red' : 'blue'}>
                        {value}
                    </Tag>
                    <span>{getUserRoleText(value)}</span>
                </Space>
            ),
        },
        {
            title: t('roleManagement.roleName'),
            dataIndex: 'roleName',
            width: 240,
        },
        {
            title: t('roleManagement.userCount'),
            dataIndex: 'userCount',
            width: 140,
        },
        {
            title: t('roleManagement.editable'),
            dataIndex: 'editable',
            width: 140,
            render: (value: boolean) =>
                value ? (
                    <Tag color="success">{t('roleManagement.editableYes')}</Tag>
                ) : (
                    <Tag>{t('roleManagement.editableNo')}</Tag>
                ),
        },
        {
            title: t('common.actions'),
            key: 'action',
            width: 100,
            render: (_, record) => (
                <Button
                    type="text"
                    size="small"
                    icon={record.editable ? <EditOutlined /> : <EyeOutlined />}
                    onClick={() => openRoleModal(record)}
                />
            ),
        },
    ];

    return (
        <div className={styles.page}>
            {contextHolder}
            <Card
                title={t('roleManagement.pageTitle')}
                extra={
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={() => void loadRoles()}
                    >
                        {t('common.refresh')}
                    </Button>
                }
            >
                <Table<BmsRoleDefinition>
                    rowKey="roleCode"
                    loading={loading}
                    columns={columns}
                    dataSource={roles}
                    pagination={false}
                />
            </Card>

            <Modal
                title={
                    editingRole?.editable
                        ? t('roleManagement.editRoleTitle')
                        : t('roleManagement.viewRoleTitle')
                }
                open={modalOpen}
                onCancel={closeRoleModal}
                onOk={() => void submitRoleUpdate()}
                confirmLoading={saving}
                okButtonProps={{ disabled: !editingRole?.editable }}
                destroyOnHidden
                maskClosable={false}
            >
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                    <div className={styles.roleInfo}>
                        <div>
                            {t('common.role')}：
                            {editingRole
                                ? getUserRoleText(editingRole.roleCode)
                                : '-'}
                        </div>
                        <div>
                            {t('roleManagement.userCount')}：
                            {editingRole?.userCount ?? '-'}
                        </div>
                    </div>

                    <Form form={form} layout="vertical">
                        <Form.Item
                            name="roleName"
                            label={t('roleManagement.roleName')}
                            rules={[
                                {
                                    required: true,
                                    message: t('roleManagement.roleNameRequired'),
                                },
                                {
                                    max: 100,
                                    message: t('roleManagement.roleNameMax'),
                                },
                            ]}
                        >
                            <Input
                                maxLength={100}
                                disabled={!editingRole?.editable}
                                placeholder={t(
                                    'roleManagement.roleNamePlaceholder',
                                )}
                            />
                        </Form.Item>
                    </Form>
                </Space>
            </Modal>
        </div>
    );
};

export default RoleManagementPage;
