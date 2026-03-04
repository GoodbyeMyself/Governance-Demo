import {
    fetchMetadataCollectionTaskDetail,
    type MetadataCollectionTaskItem,
} from '@/services/metadataCollection';
import { type DataSourceType } from '@/services/dataSource';
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from '@umijs/max';
import { Button, Card, Descriptions, Space, Spin, Tag, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

const dataSourceTypeTextMap: Record<DataSourceType, string> = {
    DATABASE: '数据库',
    FILE_SYSTEM: '文件系统',
};

const strategyTextMap: Record<MetadataCollectionTaskItem['strategy'], string> = {
    FULL: '全量采集',
    INCREMENTAL: '增量采集',
};

const scopeTextMap: Record<MetadataCollectionTaskItem['scope'], string> = {
    SCHEMA: 'Schema 级',
    TABLE: '表级',
};

const scheduleTypeTextMap: Record<MetadataCollectionTaskItem['scheduleType'], string> = {
    MANUAL: '手动触发',
    CRON: '定时 Cron',
};

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const MetadataCollectionDetailPage: React.FC = () => {
    const navigate = useNavigate();
    const params = useParams<{ id?: string }>();
    const taskId = useMemo(() => Number(params.id || 0), [params.id]);
    const [task, setTask] = useState<MetadataCollectionTaskItem | null>(null);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const loadDetail = async () => {
        if (!taskId || Number.isNaN(taskId)) {
            messageApi.error('任务 ID 无效');
            return;
        }
        setLoading(true);
        try {
            const res = await fetchMetadataCollectionTaskDetail(taskId);
            if (!res.success || !res.data) {
                throw new Error(res.message || '加载任务详情失败');
            }
            setTask(res.data);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载任务详情失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadDetail();
    }, [taskId]);

    return (
        <div className={styles.page}>
            {contextHolder}
            <div className={styles.headerAction}>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => void loadDetail()}>
                        刷新
                    </Button>
                    <Button
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate('/metadata-collection')}
                    >
                        返回列表
                    </Button>
                </Space>
            </div>
            <Card title="采集任务详情">
                <Spin spinning={loading}>
                    {task ? (
                        <Descriptions column={2} bordered>
                            <Descriptions.Item label="任务 ID">{task.id}</Descriptions.Item>
                            <Descriptions.Item label="任务名称">
                                {task.taskName}
                            </Descriptions.Item>
                            <Descriptions.Item label="数据源">
                                {task.dataSourceName}
                            </Descriptions.Item>
                            <Descriptions.Item label="数据源类型">
                                <Tag color={task.dataSourceType === 'DATABASE' ? 'blue' : 'green'}>
                                    {dataSourceTypeTextMap[task.dataSourceType]}
                                </Tag>
                            </Descriptions.Item>
                            <Descriptions.Item label="采集策略">
                                {strategyTextMap[task.strategy]}
                            </Descriptions.Item>
                            <Descriptions.Item label="采集范围">
                                {scopeTextMap[task.scope]}
                            </Descriptions.Item>
                            <Descriptions.Item label="调度方式">
                                {scheduleTypeTextMap[task.scheduleType]}
                            </Descriptions.Item>
                            <Descriptions.Item label="Cron 表达式">
                                {task.cronExpression || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="目标匹配规则" span={2}>
                                {task.targetPattern || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="启用状态">
                                {task.enabled ? (
                                    <Tag color="success">启用</Tag>
                                ) : (
                                    <Tag color="default">停用</Tag>
                                )}
                            </Descriptions.Item>
                            <Descriptions.Item label="更新时间">
                                {formatTime(task.updatedAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label="创建时间">
                                {formatTime(task.createdAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label="描述">
                                {task.description || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="采集配置（JSON）" span={2}>
                                {task.configJson ? (
                                    <pre className={styles.jsonBlock}>
                                        {task.configJson}
                                    </pre>
                                ) : (
                                    '-'
                                )}
                            </Descriptions.Item>
                        </Descriptions>
                    ) : (
                        <div>暂无详情数据</div>
                    )}
                </Spin>
            </Card>
        </div>
    );
};

export default MetadataCollectionDetailPage;
