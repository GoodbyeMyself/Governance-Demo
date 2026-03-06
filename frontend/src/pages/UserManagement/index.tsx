import {
    fetchAuthUsers,
    updateAuthUserRole,
    type AuthCenterUserProfile,
    type AuthCenterUserRole,
} from '@/services/authCenter';
import { EditOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    Button,
    Card,
    Modal,
    Select,
    Space,
    Table,
    Tag,
    Tooltip,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.module.less';

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const roleTextMap: Record<AuthCenterUserRole, string> = {
    ADMIN: '管理员',
    USER: '普通用户',
};

const roleColorMap: Record<AuthCenterUserRole, string> = {
    ADMIN: 'red',
    USER: 'blue',
};

const UserManagementPage: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [users, setUsers] = useState<AuthCenterUserProfile[]>([]);
    const [messageApi, contextHolder] = message.useMessage();

    const [roleModalOpen, setRoleModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<AuthCenterUserProfile | null>(null);
    const [nextRole, setNextRole] = useState<AuthCenterUserRole>('USER');

    const sortedUsers = useMemo(() => {
        return [...users].sort((a, b) => a.id - b.id);
    }, [users]);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const res = await fetchAuthUsers();
            if (!res.success) {
                throw new Error(res.message || '加载用户列表失败');
            }
            setUsers(res.data || []);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载用户列表失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadUsers();
    }, []);

    const openRoleModal = (user: AuthCenterUserProfile) => {
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
            const res = await updateAuthUserRole(editingUser.id, { role: nextRole });
            if (!res.success) {
                throw new Error(res.message || '更新用户角色失败');
            }
            messageApi.success('用户角色更新成功');
            closeRoleModal();
            await loadUsers();
        } catch (error) {
            const msg = error instanceof Error ? error.message : '更新用户角色失败';
            messageApi.error(msg);
        } finally {
            setSaving(false);
        }
    };

    const columns: ColumnsType<AuthCenterUserProfile> = [
        {
            title: 'ID',
            dataIndex: 'id',
            width: 80,
        },
        {
            title: '用户名',
            dataIndex: 'username',
            width: 180,
            ellipsis: true,
        },
        {
            title: '昵称',
            dataIndex: 'nickname',
            width: 160,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: '邮箱',
            dataIndex: 'email',
            width: 220,
            ellipsis: true,
            render: (value) => value || '-',
        },
        {
            title: '手机号',
            dataIndex: 'phone',
            width: 160,
            render: (value) => value || '-',
        },
        {
            title: '角色',
            dataIndex: 'role',
            width: 110,
            render: (value: AuthCenterUserRole) => (
                <Tag color={roleColorMap[value]}>{roleTextMap[value]}</Tag>
            ),
        },
        {
            title: '状态',
            dataIndex: 'status',
            width: 110,
            render: (value) =>
                value === 'ENABLED' ? (
                    <Tag color="success">启用</Tag>
                ) : (
                    <Tag color="default">禁用</Tag>
                ),
        },
        {
            title: '最近登录',
            dataIndex: 'lastLoginAt',
            width: 180,
            render: formatTime,
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            width: 180,
            render: formatTime,
        },
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 80,
            render: (_, record) => (
                <Tooltip title="修改角色">
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
                title="用户管理"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => void loadUsers()}>
                            刷新
                        </Button>
                    </Space>
                }
            >
                <Table<AuthCenterUserProfile>
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={sortedUsers}
                    scroll={{ x: 1500 }}
                    pagination={{
                        showSizeChanger: true,
                        defaultPageSize: 10,
                        showTotal: (total) => `共 ${total} 条`,
                    }}
                />
            </Card>

            <Modal
                title="修改用户角色"
                open={roleModalOpen}
                confirmLoading={saving}
                onCancel={closeRoleModal}
                onOk={() => void submitRoleUpdate()}
                destroyOnHidden={true}
                mask={{ closable: false }}
            >
                <div className={styles.userInfo}>
                    <div>用户名：{editingUser?.username || '-'}</div>
                    <div>
                        当前角色：
                        {editingUser ? roleTextMap[editingUser.role] : '-'}
                    </div>
                </div>
                <Select<AuthCenterUserRole>
                    value={nextRole}
                    style={{ width: '100%' }}
                    options={[
                        { label: '管理员', value: 'ADMIN' },
                        { label: '普通用户', value: 'USER' },
                    ]}
                    onChange={(value) => setNextRole(value)}
                />
            </Modal>
        </div>
    );
};

export default UserManagementPage;
