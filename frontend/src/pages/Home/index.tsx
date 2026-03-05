import {
    fetchWorkbenchOverview,
    type WorkbenchDailyTrendItem,
    type WorkbenchOverview,
} from '@/services/workbench';
import {
    ArrowRightOutlined,
    DatabaseOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SettingOutlined,
} from '@ant-design/icons';
import { useNavigate } from '@umijs/max';
import {
    Button,
    Card,
    Col,
    Progress,
    Row,
    Space,
    Statistic,
    Table,
    Tag,
    message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

const formatTime = (value?: string) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

const formatShortDate = (value: string) => {
    if (!value) return '-';
    return value.slice(5);
};

const calcMaxTrendCount = (data: WorkbenchDailyTrendItem[]) => {
    if (!data.length) return 1;
    const max = Math.max(...data.map((item) => item.count || 0));
    return max > 0 ? max : 1;
};

const HomePage: React.FC = () => {
    const navigate = useNavigate();
    const [overview, setOverview] = useState<WorkbenchOverview | null>(null);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const enabledTaskRate = useMemo(() => {
        const total = overview?.totalMetadataTasks || 0;
        if (!total) return 0;
        const enabled = overview?.enabledMetadataTaskCount || 0;
        return Math.round((enabled / total) * 100);
    }, [overview?.totalMetadataTasks, overview?.enabledMetadataTaskCount]);

    const dataSourceTrendMax = useMemo(
        () => calcMaxTrendCount(overview?.dataSourceUpdateTrend7d || []),
        [overview?.dataSourceUpdateTrend7d],
    );
    const metadataTrendMax = useMemo(
        () => calcMaxTrendCount(overview?.metadataTaskUpdateTrend7d || []),
        [overview?.metadataTaskUpdateTrend7d],
    );

    const loadOverview = async () => {
        setLoading(true);
        try {
            const res = await fetchWorkbenchOverview();
            if (!res.success || !res.data) {
                throw new Error(res.message || '加载工作台数据失败');
            }
            setOverview(res.data);
        } catch (error) {
            const msg = error instanceof Error ? error.message : '加载工作台数据失败';
            messageApi.error(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadOverview();
    }, []);

    const recentDataSourceColumns: ColumnsType<
        NonNullable<WorkbenchOverview['recentDataSources']>[number]
    > = [
        {
            title: '数据源名称',
            dataIndex: 'name',
            ellipsis: true,
        },
        {
            title: '类型',
            dataIndex: 'type',
            width: 120,
            render: (value) =>
                value === 'DATABASE' ? (
                    <Tag color="blue">数据库</Tag>
                ) : (
                    <Tag color="green">文件系统</Tag>
                ),
        },
        {
            title: '更新时间',
            dataIndex: 'updatedAt',
            width: 180,
            render: formatTime,
        },
    ];

    const recentTaskColumns: ColumnsType<
        NonNullable<WorkbenchOverview['recentMetadataTasks']>[number]
    > = [
        {
            title: '任务名称',
            dataIndex: 'taskName',
            ellipsis: true,
        },
        {
            title: '数据源',
            dataIndex: 'dataSourceName',
            ellipsis: true,
            width: 160,
        },
        {
            title: '策略',
            dataIndex: 'strategy',
            width: 90,
            render: (value) => (value === 'FULL' ? '全量' : '增量'),
        },
        {
            title: '调度',
            dataIndex: 'scheduleType',
            width: 100,
            render: (value) => (value === 'CRON' ? 'Cron' : '手动'),
        },
        {
            title: '状态',
            dataIndex: 'enabled',
            width: 90,
            render: (value) =>
                value ? <Tag color="success">启用</Tag> : <Tag>停用</Tag>,
        },
        {
            title: '更新时间',
            dataIndex: 'updatedAt',
            width: 180,
            render: formatTime,
        },
    ];

    return (
        <div className={styles.page}>
            {contextHolder}

            <Row gutter={[16, 16]} className={styles.topRow}>
                <Col xs={24} xl={16}>
                    <Card className={styles.headerCard}>
                        <div className={styles.headerMain}>
                            <div>
                                <div className={styles.headerTitle}>系统工作台</div>
                                <div className={styles.headerDesc}>
                                    统一查看数据源、采集任务和近期更新趋势
                                </div>
                            </div>
                            <Button
                                icon={<ReloadOutlined />}
                                loading={loading}
                                onClick={() => void loadOverview()}
                            >
                                刷新统计
                            </Button>
                        </div>

                        <Row gutter={[12, 12]} className={styles.statGrid}>
                            <Col xs={24} sm={12} lg={6}>
                                <Card className={styles.statCard} bordered={false}>
                                    <Statistic
                                        title="数据源总数"
                                        value={overview?.totalDataSources || 0}
                                        prefix={<DatabaseOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card className={styles.statCard} bordered={false}>
                                    <Statistic
                                        title="采集任务总数"
                                        value={overview?.totalMetadataTasks || 0}
                                        prefix={<SettingOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card className={styles.statCard} bordered={false}>
                                    <Statistic
                                        title="启用任务数"
                                        value={overview?.enabledMetadataTaskCount || 0}
                                        valueStyle={{ color: '#3f8600' }}
                                        prefix={<ScheduleOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card className={styles.statCard} bordered={false}>
                                    <Statistic
                                        title="Cron 任务数"
                                        value={overview?.cronMetadataTaskCount || 0}
                                        valueStyle={{ color: '#1677ff' }}
                                        prefix={<ScheduleOutlined />}
                                    />
                                </Card>
                            </Col>
                        </Row>
                    </Card>
                </Col>

                <Col xs={24} xl={8}>
                    <Card className={styles.quickCard} title="快捷入口">
                        <div className={styles.quickActions}>
                            <Button
                                type="primary"
                                className={`${styles.quickButton} ${styles.quickButtonPrimary}`}
                                onClick={() => navigate('/data-source')}
                            >
                                <span className={styles.quickButtonText}>
                                    <span className={styles.quickButtonTitle}>
                                        进入数据源管理
                                    </span>
                                    <span className={styles.quickButtonDesc}>
                                        维护连接配置与更新记录
                                    </span>
                                </span>
                                <ArrowRightOutlined />
                            </Button>
                            <Button
                                className={styles.quickButton}
                                onClick={() => navigate('/metadata-collection')}
                            >
                                <span className={styles.quickButtonText}>
                                    <span className={styles.quickButtonTitle}>
                                        进入元数据采集
                                    </span>
                                    <span className={styles.quickButtonDesc}>
                                        新建并管理采集任务
                                    </span>
                                </span>
                                <ArrowRightOutlined />
                            </Button>
                        </div>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24}>
                    <Card title="采集运行概览">
                        <div className={styles.progressWrap}>
                            <div className={styles.progressTitle}>任务启用率</div>
                            <Progress
                                percent={enabledTaskRate}
                                status="active"
                                strokeColor="#52c41a"
                            />
                        </div>
                        <Space wrap size={[8, 8]}>
                            <Tag color="blue">
                                全量任务：{overview?.fullMetadataTaskCount || 0}
                            </Tag>
                            <Tag color="purple">
                                增量任务：{overview?.incrementalMetadataTaskCount || 0}
                            </Tag>
                            <Tag color="gold">
                                手动任务：
                                {(overview?.totalMetadataTasks || 0) -
                                    (overview?.cronMetadataTaskCount || 0)}
                            </Tag>
                            <Tag color="geekblue">
                                Cron 任务：{overview?.cronMetadataTaskCount || 0}
                            </Tag>
                        </Space>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24} lg={12}>
                    <Card title="数据源近 7 天更新趋势">
                        <div className={styles.trendList}>
                            {(overview?.dataSourceUpdateTrend7d || []).map((item) => (
                                <div className={styles.trendRow} key={item.date}>
                                    <div className={styles.trendDate}>
                                        {formatShortDate(item.date)}
                                    </div>
                                    <div className={styles.trendBarWrap}>
                                        <div
                                            className={styles.trendBarDataSource}
                                            style={{
                                                width: `${Math.max(
                                                    6,
                                                    Math.round(
                                                        ((item.count || 0) /
                                                            dataSourceTrendMax) *
                                                            100,
                                                    ),
                                                )}%`,
                                            }}
                                        />
                                    </div>
                                    <div className={styles.trendCount}>{item.count}</div>
                                </div>
                            ))}
                        </div>
                    </Card>
                </Col>
                <Col xs={24} lg={12}>
                    <Card title="采集任务近 7 天更新趋势">
                        <div className={styles.trendList}>
                            {(overview?.metadataTaskUpdateTrend7d || []).map((item) => (
                                <div className={styles.trendRow} key={item.date}>
                                    <div className={styles.trendDate}>
                                        {formatShortDate(item.date)}
                                    </div>
                                    <div className={styles.trendBarWrap}>
                                        <div
                                            className={styles.trendBarTask}
                                            style={{
                                                width: `${Math.max(
                                                    6,
                                                    Math.round(
                                                        ((item.count || 0) /
                                                            metadataTrendMax) *
                                                            100,
                                                    ),
                                                )}%`,
                                            }}
                                        />
                                    </div>
                                    <div className={styles.trendCount}>{item.count}</div>
                                </div>
                            ))}
                        </div>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24} xl={12}>
                    <Card title="最近更新的数据源">
                        <Table
                            rowKey="id"
                            size="small"
                            loading={loading}
                            columns={recentDataSourceColumns}
                            dataSource={overview?.recentDataSources || []}
                            pagination={false}
                        />
                    </Card>
                </Col>
                <Col xs={24} xl={12}>
                    <Card title="最近更新的采集任务">
                        <Table
                            rowKey="id"
                            size="small"
                            loading={loading}
                            columns={recentTaskColumns}
                            dataSource={overview?.recentMetadataTasks || []}
                            pagination={false}
                            scroll={{ x: 720 }}
                        />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default HomePage;
