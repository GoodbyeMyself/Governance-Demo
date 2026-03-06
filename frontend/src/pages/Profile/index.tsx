import { fetchCurrentUser, type AuthCenterUserProfile } from '@/services/authCenter';
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Descriptions, Space, Tag, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

const roleTextMap = {
    ADMIN: '管理员',
    USER: '普通用户',
} as const;

const statusTextMap = {
    ENABLED: '启用',
    DISABLED: '禁用',
} as const;

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const ProfilePage: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [profile, setProfile] = useState<AuthCenterUserProfile | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const loadProfile = async () => {
        setLoading(true);
        try {
            const res = await fetchCurrentUser();
            if (!res.success || !res.data) {
                throw new Error(res.message || '加载个人信息失败');
            }
            setProfile(res.data);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载个人信息失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadProfile();
    }, []);

    return (
        <div className={styles.page}>
            {contextHolder}
            <div className={styles.header}>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => void loadProfile()}>
                        刷新
                    </Button>
                    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/home')}>
                        返回工作台
                    </Button>
                </Space>
            </div>

            <Card title="个人中心" loading={loading}>
                <Descriptions bordered column={2}>
                    <Descriptions.Item label="用户 ID">
                        {profile?.id ?? '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="用户名">
                        {profile?.username || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="昵称">
                        {profile?.nickname || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="角色">
                        {profile?.role ? (
                            <Tag color={profile.role === 'ADMIN' ? 'red' : 'blue'}>
                                {roleTextMap[profile.role]}
                            </Tag>
                        ) : (
                            '-'
                        )}
                    </Descriptions.Item>
                    <Descriptions.Item label="状态">
                        {profile?.status ? (
                            <Tag color={profile.status === 'ENABLED' ? 'success' : 'default'}>
                                {statusTextMap[profile.status]}
                            </Tag>
                        ) : (
                            '-'
                        )}
                    </Descriptions.Item>
                    <Descriptions.Item label="最近登录">
                        {formatTime(profile?.lastLoginAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="邮箱">
                        {profile?.email || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="手机号">
                        {profile?.phone || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="创建时间" span={2}>
                        {formatTime(profile?.createdAt)}
                    </Descriptions.Item>
                </Descriptions>
            </Card>
        </div>
    );
};

export default ProfilePage;
