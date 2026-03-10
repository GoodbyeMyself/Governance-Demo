import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    fetchCurrentUser,
    updateCurrentUserProfile,
    type AuthCenterUserProfile,
} from '@governance/api';
import { RoleTag, UserStatusTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    HOME_PATH,
    formatDateTime,
    getAuthPersistence,
    setAuthState,
} from '@governance/utils';
import {
    Button,
    Card,
    Descriptions,
    Form,
    Input,
    Space,
    message,
} from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './index.module.less';

type ProfileFormValues = {
    username: string;
    email: string;
    phone?: string;
};

const ProfilePage: React.FC = () => {
    const { t } = useI18n();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [profile, setProfile] = useState<AuthCenterUserProfile | null>(null);
    const [form] = Form.useForm<ProfileFormValues>();
    const [messageApi, contextHolder] = message.useMessage();

    const loadProfile = async () => {
        setLoading(true);
        try {
            const response = await fetchCurrentUser();
            if (!response.success || !response.data) {
                throw new Error(response.message || t('profile.loadFailed'));
            }

            setProfile(response.data);
            form.setFieldsValue({
                username: response.data.username,
                email: response.data.email || '',
                phone: response.data.phone || '',
            });
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

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            const response = await updateCurrentUserProfile({
                username: values.username.trim(),
                email: values.email.trim(),
                phone: values.phone?.trim() || undefined,
            });
            if (!response.success || !response.data?.user) {
                throw new Error(
                    response.message || t('profile.updateProfileFailed'),
                );
            }

            const storageOptions = {
                persistence: getAuthPersistence(),
            };
            setAuthState(
                '',
                response.data.user,
                storageOptions,
            );
            setProfile(response.data.user);
            form.setFieldsValue({
                username: response.data.user.username,
                email: response.data.user.email || '',
                phone: response.data.user.phone || '',
            });
            messageApi.success(t('profile.updateProfileSuccess'));
        } catch (error) {
            if (error && typeof error === 'object' && 'errorFields' in error) {
                return;
            }

            const messageText =
                error instanceof Error
                    ? error.message
                    : t('profile.updateProfileFailed');
            messageApi.error(messageText);
        } finally {
            setSaving(false);
        }
    };

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

            <div className={styles.grid}>
                <Card
                    title={t('profile.pageTitle')}
                    loading={loading}
                    className={styles.summaryCard}
                >
                    <Descriptions bordered column={1}>
                        <Descriptions.Item label={t('profile.userId')}>
                            {profile?.id ?? '-'}
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
                        <Descriptions.Item label={t('common.createdAt')}>
                            {formatDateTime(profile?.createdAt)}
                        </Descriptions.Item>
                    </Descriptions>
                </Card>

                <Card
                    title={t('profile.editProfileTitle')}
                    loading={loading}
                    className={styles.formCard}
                >
                    <Form form={form} layout="vertical">
                        <Form.Item
                            name="username"
                            label={t('auth.username')}
                            rules={[
                                {
                                    required: true,
                                    message: t('auth.usernameRequired'),
                                },
                                {
                                    min: 4,
                                    max: 64,
                                    message: t('profile.usernameLength'),
                                },
                                {
                                    pattern: /^[a-zA-Z0-9_]+$/,
                                    message: t('profile.usernamePattern'),
                                },
                            ]}
                        >
                            <Input placeholder={t('auth.usernamePlaceholder')} />
                        </Form.Item>

                        <Form.Item
                            name="email"
                            label={t('auth.email')}
                            rules={[
                                {
                                    required: true,
                                    message: t('profile.emailRequired'),
                                },
                                {
                                    type: 'email',
                                    message: t('auth.invalidEmail'),
                                },
                                {
                                    max: 100,
                                    message: t('auth.emailMax'),
                                },
                            ]}
                        >
                            <Input placeholder={t('profile.emailPlaceholder')} />
                        </Form.Item>

                        <Form.Item
                            name="phone"
                            label={t('auth.phone')}
                            rules={[
                                {
                                    pattern: /^$|^[0-9+\-]{6,30}$/,
                                    message: t('auth.invalidPhone'),
                                },
                                {
                                    max: 30,
                                    message: t('auth.phoneMax'),
                                },
                            ]}
                        >
                            <Input placeholder={t('profile.phonePlaceholder')} />
                        </Form.Item>

                        <Space>
                            <Button
                                type="primary"
                                loading={saving}
                                onClick={() => void handleSubmit()}
                            >
                                {t('common.save')}
                            </Button>
                            <Button onClick={() => form.resetFields()}>
                                {t('common.cancel')}
                            </Button>
                        </Space>
                    </Form>
                </Card>
            </div>
        </div>
    );
};

export default ProfilePage;
