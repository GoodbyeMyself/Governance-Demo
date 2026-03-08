import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { fetchCurrentUser, type AuthCenterUserProfile } from '@governance/api';
import { RoleTag, UserStatusTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import { HOME_PATH, formatDateTime } from '@governance/utils';
import { Button, Card, Descriptions, Space, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

const ProfilePage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [profile, setProfile] = useState<AuthCenterUserProfile | null>(null);
    const [messageApi, contextHolder] = message.useMessage();

    const loadProfile = async () => {
        setLoading(true);
        try {
            const response = await fetchCurrentUser();
            if (!response.success || !response.data) {
                throw new Error(response.message || t('profile.loadFailed'));
            }
            setProfile(response.data);
        } catch (error) {
            const messageText =
                error instanceof Error ? error.message : t('profile.loadFailed');
            messageApi.error(messageText);
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
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={() => void loadProfile()}
                    >
                        {t('common.refresh')}
                    </Button>
                    <Button
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate(HOME_PATH)}
                    >
                        {t('common.backToHome')}
                    </Button>
                </Space>
            </div>

            <Card title={t('profile.pageTitle')} loading={loading}>
                <Descriptions bordered column={2}>
                    <Descriptions.Item label={t('profile.userId')}>
                        {profile?.id ?? '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('auth.username')}>
                        {profile?.username || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('auth.nickname')}>
                        {profile?.nickname || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('common.role')}>
                        <RoleTag role={profile?.role} />
                    </Descriptions.Item>
                    <Descriptions.Item label={t('common.status')}>
                        <UserStatusTag status={profile?.status} />
                    </Descriptions.Item>
                    <Descriptions.Item label={t('profile.lastLoginAt')}>
                        {formatDateTime(profile?.lastLoginAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('auth.email')}>
                        {profile?.email || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('auth.phone')}>
                        {profile?.phone || '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label={t('common.createdAt')} span={2}>
                        {formatDateTime(profile?.createdAt)}
                    </Descriptions.Item>
                </Descriptions>
            </Card>
        </div>
    );
};

export default ProfilePage;
