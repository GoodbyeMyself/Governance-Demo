import { EditOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchBmsRoles,
    fetchBmsUsers,
    updateBmsUserRole,
    type BmsUserProfile,
    type BmsUserRole,
} from '@governance/api';
import { RoleTag, UserStatusTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    ROLE_MANAGEMENT_PATH,
    formatDateTime,
    getUserRoleText,
} from '@governance/utils';
import { Button, Card, Modal, Select, Space, Table, Tooltip, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

const DEFAULT_ROLE_OPTIONS: BmsUserRole[] = ['ADMIN', 'USER'];

const UserManagementPage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [users, setUsers] = useState<BmsUserProfile[]>([]);
    const [availableRoles, setAvailableRoles] =
        useState<BmsUserRole[]>(DEFAULT_ROLE_OPTIONS);
    const [messageApi, contextHolder] = message.useMessage();
    const [roleModalOpen, setRoleModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<BmsUserProfile | null>(null);
    const [nextRole, setNextRole] = useState<BmsUserRole>('USER');

    const sortedUsers = useMemo(
        () => [...users].sort((a, b) => a.id - b.id),
        [users],
    );

    const loadUsers = async () => {
        setLoading(true);
        try {
            const response = await fetchBmsUsers();
            if (!response.success) {
                throw new Error(
                    response.message || t('userManagement.loadUsersFailed'),
                );
            }
            setUsers(response.data || []);
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('userManagement.loadUsersFailed');
            messageApi.error(messageText);
        } finally {
            setLoading(false);
        }
    };

    const loadRoles = async () => {
        try {
            const response = await fetchBmsRoles();
            if (!response.success) {
                throw new Error(
                    response.message || t('userManagement.loadRolesFailed'),
                );
            }
            setAvailableRoles(
                response.data && response.data.length > 0
                    ? response.data
                    : DEFAULT_ROLE_OPTIONS,
            );
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('userManagement.loadRolesFailed');
            messageApi.warning(messageText);
            setAvailableRoles(DEFAULT_ROLE_OPTIONS);
        }
    };

    useEffect(() => {
        void Promise.all([loadUsers(), loadRoles()]);
    }, []);

    const openRoleModal = (user: BmsUserProfile) => {
        setEditingUser(user);
        setNextRole(user.role);
        setRoleModalOpen(true);
    };

    const closeRoleModal = () => {
        setRoleModalOpen(false);
        setEditingUser(null);
        setNextRole('USER');
    };

    const submitRoleUpdate = async () => {
        if (!editingUser) {
            return;
        }

        if (editingUser.role === nextRole) {
            closeRoleModal();
            return;
        }

        setSaving(true);
        try {
            const response = await updateBmsUserRole(editingUser.id, {
                role: nextRole,
            });
            if (!response.success) {
                throw new Error(
                    response.message || t('userManagement.updateRoleFailed'),
                );
            }
            messageApi.success(t('userManagement.updateRoleSuccess'));
            closeRoleModal();
            await loadUsers();
        } catch (error) {
            const messageText =
                error instanceof Error
                    ? error.message
                    : t('userManagement.updateRoleFailed');
            messageApi.error(messageText);
        } finally {
            setSaving(false);
        }
    };

    const columns: ColumnsType<BmsUserProfile> = [
        { title: 'ID', dataIndex: 'id', width: 80 },
        {
            title: t('auth.username'),
            dataIndex: 'username',
            width: 180,
            ellipsis: true,
        },
        {
            title: t('auth.nickname'),
            dataIndex: 'nickname',
            width: 160,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: t('auth.email'),
            dataIndex: 'email',
            width: 220,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: t('auth.phone'),
            dataIndex: 'phone',
            width: 160,
            render: (value) => value || '-',
        },
        {
            title: t('common.role'),
            dataIndex: 'role',
            width: 110,
            render: (value: BmsUserRole) => <RoleTag role={value} />,
        },
        {
            title: t('common.status'),
            dataIndex: 'status',
            width: 110,
            render: (value) => <UserStatusTag status={value} />,
        },
        {
            title: t('profile.lastLoginAt'),
            dataIndex: 'lastLoginAt',
            width: 180,
            render: formatDateTime,
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
            width: 80,
            render: (_, record) => (
                <Tooltip title={t('userManagement.changeRole')}>
                    <Button
                        type="text"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => openRoleModal(record)}
                    />
                </Tooltip>
            ),
        },
    ];

    return (
        <div className={styles.page}>
            {contextHolder}
            <Card
                title={t('userManagement.pageTitle')}
                extra={
                    <Space>
                        <Button onClick={() => navigate(ROLE_MANAGEMENT_PATH)}>
                            {t('userManagement.goToRoleManagement')}
                        </Button>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() =>
                                void Promise.all([loadUsers(), loadRoles()])
                            }
                        >
                            {t('common.refresh')}
                        </Button>
                    </Space>
                }
            >
                <Table<BmsUserProfile>
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={sortedUsers}
                    scroll={{ x: 1500 }}
                    pagination={{
                        showSizeChanger: true,
                        defaultPageSize: 10,
                        showTotal: (total) => t('common.totalRows', { total }),
                    }}
                />
            </Card>

            <Modal
                title={t('userManagement.editRoleTitle')}
                open={roleModalOpen}
                confirmLoading={saving}
                onCancel={closeRoleModal}
                onOk={() => void submitRoleUpdate()}
                destroyOnHidden
                maskClosable={false}
            >
                <div className={styles.userInfo}>
                    <div>
                        {t('auth.username')}：{editingUser?.username || '-'}
                    </div>
                    <div>
                        {t('userManagement.currentRole')}：
                        {editingUser ? getUserRoleText(editingUser.role) : '-'}
                    </div>
                </div>
                <Select<BmsUserRole>
                    value={nextRole}
                    style={{ width: '100%' }}
                    options={availableRoles.map((role) => ({
                        label: getUserRoleText(role),
                        value: role,
                    }))}
                    onChange={(value) => setNextRole(value)}
                />
            </Modal>
        </div>
    );
};

export default UserManagementPage;
