import {
    ArrowRightOutlined,
    DatabaseOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SettingOutlined,
} from '@ant-design/icons';
import {
    fetchWorkbenchOverview,
    type WorkbenchDailyTrendItem,
    type WorkbenchOverview,
} from '@governance/api';
import { DataSourceTypeTag, EnabledTag } from '@governance/components';
import { useI18n } from '@governance/i18n';
import {
    formatDateTime,
    formatMonthDay,
    getMetadataScheduleTypeText,
    getMetadataStrategyText,
} from '@governance/utils';
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

type RecentDataSourceItem =
    NonNullable<WorkbenchOverview['recentDataSources']>[number];
type RecentMetadataTaskItem =
    NonNullable<WorkbenchOverview['recentMetadataTasks']>[number];

export interface WorkbenchOverviewDashboardProps {
    className?: string;
    headerTitle: string;
    headerDescription: string;
    showQuickActions?: boolean;
    quickActionTitle?: string;
    onDataSourceClick?: () => void;
    onMetadataCollectionClick?: () => void;
    styles: Record<string, string>;
}

const calcMaxTrendCount = (data: WorkbenchDailyTrendItem[]) => {
    if (!data.length) return 1;
    const max = Math.max(...data.map((item) => item.count || 0));
    return max > 0 ? max : 1;
};

export const WorkbenchOverviewDashboard: React.FC<
    WorkbenchOverviewDashboardProps
> = ({
    className,
    headerTitle,
    headerDescription,
    showQuickActions = false,
    quickActionTitle,
    onDataSourceClick,
    onMetadataCollectionClick,
    styles,
}) => {
    const { t } = useI18n();
    const [overview, setOverview] = useState<WorkbenchOverview | null>(null);
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

    const enabledTaskRate = useMemo(() => {
        const total = overview?.totalMetadataTasks || 0;
        if (!total) return 0;
        const enabled = overview?.enabledMetadataTaskCount || 0;
        return Math.round((enabled / total) * 100);
    }, [overview?.enabledMetadataTaskCount, overview?.totalMetadataTasks]);

    const manualTaskCount = useMemo(
        () =>
            Math.max(
                0,
                (overview?.totalMetadataTasks || 0) -
                    (overview?.cronMetadataTaskCount || 0),
            ),
        [overview?.cronMetadataTaskCount, overview?.totalMetadataTasks],
    );

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
            const response = await fetchWorkbenchOverview();
            if (!response.success || !response.data) {
                throw new Error(response.message || t('http.default'));
            }
            setOverview(response.data);
        } catch (error) {
            const messageText =
                error instanceof Error ? error.message : t('http.default');
            messageApi.error(messageText);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadOverview();
    }, []);

    const recentDataSourceColumns: ColumnsType<RecentDataSourceItem> = [
        {
            title: t('dashboard.dataSourceName'),
            dataIndex: 'name',
            ellipsis: true,
        },
        {
            title: t('common.type'),
            dataIndex: 'type',
            width: 120,
            render: (value) => <DataSourceTypeTag type={value} />,
        },
        {
            title: t('dashboard.updatedAt'),
            dataIndex: 'updatedAt',
            width: 180,
            render: formatDateTime,
        },
    ];

    const recentTaskColumns: ColumnsType<RecentMetadataTaskItem> = [
        {
            title: t('dashboard.taskName'),
            dataIndex: 'taskName',
            ellipsis: true,
        },
        {
            title: t('metadata.field.dataSource'),
            dataIndex: 'dataSourceName',
            ellipsis: true,
            width: 160,
        },
        {
            title: t('dashboard.strategy'),
            dataIndex: 'strategy',
            width: 120,
            render: getMetadataStrategyText,
        },
        {
            title: t('dashboard.scheduleType'),
            dataIndex: 'scheduleType',
            width: 120,
            render: getMetadataScheduleTypeText,
        },
        {
            title: t('common.status'),
            dataIndex: 'enabled',
            width: 100,
            render: (value: boolean) => <EnabledTag enabled={value} />,
        },
        {
            title: t('dashboard.updatedAt'),
            dataIndex: 'updatedAt',
            width: 180,
            render: formatDateTime,
        },
    ];

    return (
        <div className={`${styles.page} ${className || ''}`.trim()}>
            {contextHolder}

            <Row gutter={[16, 16]} className={styles.topRow}>
                <Col xs={24} xl={showQuickActions ? 16 : 24}>
                    <Card className={styles.headerCard}>
                        <div className={styles.headerMain}>
                            <div>
                                <div className={styles.headerTitle}>
                                    {headerTitle}
                                </div>
                                <div className={styles.headerDesc}>
                                    {headerDescription}
                                </div>
                            </div>
                            <Button
                                icon={<ReloadOutlined />}
                                loading={loading}
                                onClick={() => void loadOverview()}
                            >
                                {t('common.refresh')}
                            </Button>
                        </div>

                        <Row gutter={[12, 12]} className={styles.statGrid}>
                            <Col xs={24} sm={12} lg={6}>
                                <Card
                                    className={styles.statCard}
                                    variant="borderless"
                                >
                                    <Statistic
                                        title={t('dashboard.stats.totalDataSources')}
                                        value={overview?.totalDataSources || 0}
                                        prefix={<DatabaseOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card
                                    className={styles.statCard}
                                    variant="borderless"
                                >
                                    <Statistic
                                        title={t(
                                            'dashboard.stats.totalMetadataTasks',
                                        )}
                                        value={
                                            overview?.totalMetadataTasks || 0
                                        }
                                        prefix={<SettingOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card
                                    className={styles.statCard}
                                    variant="borderless"
                                >
                                    <Statistic
                                        title={t(
                                            'dashboard.stats.enabledMetadataTasks',
                                        )}
                                        value={
                                            overview?.enabledMetadataTaskCount ||
                                            0
                                        }
                                        styles={{
                                            content: { color: '#3f8600' },
                                        }}
                                        prefix={<ScheduleOutlined />}
                                    />
                                </Card>
                            </Col>
                            <Col xs={24} sm={12} lg={6}>
                                <Card
                                    className={styles.statCard}
                                    variant="borderless"
                                >
                                    <Statistic
                                        title={t(
                                            'dashboard.stats.cronMetadataTasks',
                                        )}
                                        value={
                                            overview?.cronMetadataTaskCount || 0
                                        }
                                        styles={{
                                            content: { color: '#1677ff' },
                                        }}
                                        prefix={<ScheduleOutlined />}
                                    />
                                </Card>
                            </Col>
                        </Row>
                    </Card>
                </Col>

                {showQuickActions ? (
                    <Col xs={24} xl={8}>
                        <Card
                            className={styles.quickCard}
                            title={quickActionTitle || t('dashboard.quickActions')}
                        >
                            <div className={styles.quickActions}>
                                <Button
                                    type="primary"
                                    className={`${styles.quickButton} ${styles.quickButtonPrimary}`}
                                    onClick={onDataSourceClick}
                                >
                                    <span className={styles.quickButtonText}>
                                        <span
                                            className={styles.quickButtonTitle}
                                        >
                                            {t(
                                                'dashboard.quickAction.dataSource.title',
                                            )}
                                        </span>
                                        <span
                                            className={styles.quickButtonDesc}
                                        >
                                            {t(
                                                'dashboard.quickAction.dataSource.desc',
                                            )}
                                        </span>
                                    </span>
                                    <ArrowRightOutlined />
                                </Button>
                                <Button
                                    className={styles.quickButton}
                                    onClick={onMetadataCollectionClick}
                                >
                                    <span className={styles.quickButtonText}>
                                        <span
                                            className={styles.quickButtonTitle}
                                        >
                                            {t(
                                                'dashboard.quickAction.metadata.title',
                                            )}
                                        </span>
                                        <span
                                            className={styles.quickButtonDesc}
                                        >
                                            {t(
                                                'dashboard.quickAction.metadata.desc',
                                            )}
                                        </span>
                                    </span>
                                    <ArrowRightOutlined />
                                </Button>
                            </div>
                        </Card>
                    </Col>
                ) : null}
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24}>
                    <Card title={t('dashboard.runtimeOverview')}>
                        <div className={styles.progressWrap}>
                            <div className={styles.progressTitle}>
                                {t('dashboard.enabledRate')}
                            </div>
                            <Progress
                                percent={enabledTaskRate}
                                status="active"
                                strokeColor="#52c41a"
                            />
                        </div>
                        <Space wrap size={[8, 8]}>
                            <Tag color="blue">
                                {t('dashboard.fullTasks', {
                                    count: overview?.fullMetadataTaskCount || 0,
                                })}
                            </Tag>
                            <Tag color="purple">
                                {t('dashboard.incrementalTasks', {
                                    count:
                                        overview?.incrementalMetadataTaskCount ||
                                        0,
                                })}
                            </Tag>
                            <Tag color="gold">
                                {t('dashboard.manualTasks', {
                                    count: manualTaskCount,
                                })}
                            </Tag>
                            <Tag color="geekblue">
                                {t('dashboard.cronTasks', {
                                    count:
                                        overview?.cronMetadataTaskCount || 0,
                                })}
                            </Tag>
                        </Space>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24} lg={12}>
                    <Card title={t('dashboard.dataSourceTrend')}>
                        <div className={styles.trendList}>
                            {(overview?.dataSourceUpdateTrend7d || []).map(
                                (item) => (
                                    <div
                                        className={styles.trendRow}
                                        key={item.date}
                                    >
                                        <div className={styles.trendDate}>
                                            {formatMonthDay(item.date)}
                                        </div>
                                        <div className={styles.trendBarWrap}>
                                            <div
                                                className={
                                                    styles.trendBarDataSource
                                                }
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
                                        <div className={styles.trendCount}>
                                            {item.count}
                                        </div>
                                    </div>
                                ),
                            )}
                        </div>
                    </Card>
                </Col>
                <Col xs={24} lg={12}>
                    <Card title={t('dashboard.metadataTrend')}>
                        <div className={styles.trendList}>
                            {(overview?.metadataTaskUpdateTrend7d || []).map(
                                (item) => (
                                    <div
                                        className={styles.trendRow}
                                        key={item.date}
                                    >
                                        <div className={styles.trendDate}>
                                            {formatMonthDay(item.date)}
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
                                        <div className={styles.trendCount}>
                                            {item.count}
                                        </div>
                                    </div>
                                ),
                            )}
                        </div>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} className={styles.contentRow}>
                <Col xs={24} xl={12}>
                    <Card title={t('dashboard.recentDataSources')}>
                        <Table<RecentDataSourceItem>
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
                    <Card title={t('dashboard.recentTasks')}>
                        <Table<RecentMetadataTaskItem>
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
