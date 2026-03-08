import { useI18n } from '@governance/i18n';
import { HOME_PATH, buildGovernAppUrl } from '@governance/utils';
import {
    Alert,
    Button,
    Card,
    Col,
    List,
    Row,
    Space,
    Statistic,
    Table,
    Timeline,
    Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import styles from './index.module.less';

type DemoMetricRow = {
    key: string;
    metric: string;
    value: string;
    trend: string;
    positive?: boolean;
};

const PortalDemoPage: React.FC = () => {
    const { t } = useI18n();

    const metricRows: DemoMetricRow[] = [
        {
            key: 'metadata',
            metric: t('portalDemo.table.metric.metadata'),
            value: '98.2%',
            trend: '+2.6%',
            positive: true,
        },
        {
            key: 'quality',
            metric: t('portalDemo.table.metric.quality'),
            value: '96.7%',
            trend: '+1.4%',
            positive: true,
        },
        {
            key: 'lineage',
            metric: t('portalDemo.table.metric.lineage'),
            value: '91.3%',
            trend: '+3.1%',
            positive: true,
        },
        {
            key: 'alerts',
            metric: t('portalDemo.table.metric.alerts'),
            value: '7',
            trend: '-5',
            positive: true,
        },
    ];

    const columns: ColumnsType<DemoMetricRow> = [
        {
            title: t('portalDemo.table.column.metric'),
            dataIndex: 'metric',
            key: 'metric',
        },
        {
            title: t('portalDemo.table.column.value'),
            dataIndex: 'value',
            key: 'value',
            render: (value: string) => (
                <span className={styles.valueCell}>{value}</span>
            ),
        },
        {
            title: t('portalDemo.table.column.trend'),
            dataIndex: 'trend',
            key: 'trend',
            render: (trend: string, record) => (
                <span
                    className={
                        record.positive ? styles.trendUp : styles.trendDown
                    }
                >
                    {trend}
                </span>
            ),
        },
    ];

    const highlights = [
        t('portalDemo.list.item.rules'),
        t('portalDemo.list.item.alertCenter'),
        t('portalDemo.list.item.lineage'),
    ];

    return (
        <div className={styles.page}>
            <Card className={styles.heroCard}>
                <Space direction="vertical" size={4}>
                    <Typography.Title level={3} style={{ margin: 0 }}>
                        {t('portalDemo.pageTitle')}
                    </Typography.Title>
                    <Typography.Paragraph
                        type="secondary"
                        style={{ marginBottom: 0 }}
                    >
                        {t('portalDemo.pageDescription')}
                    </Typography.Paragraph>
                </Space>

                <Alert
                    showIcon
                    type="info"
                    message={t('portalDemo.notice.title')}
                    description={t('portalDemo.notice.description')}
                />

                <div className={styles.heroActions}>
                    <Button type="primary">
                        {t('portalDemo.actions.primary')}
                    </Button>
                    <Button
                        onClick={() =>
                            window.location.assign(buildGovernAppUrl(HOME_PATH))
                        }
                    >
                        {t('portalDemo.actions.secondary')}
                    </Button>
                </div>
            </Card>

            <Row gutter={[16, 16]}>
                <Col xs={24} sm={12} xl={6}>
                    <Card>
                        <Statistic
                            title={t('portalDemo.stats.coverage')}
                            value={94.6}
                            suffix="%"
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} xl={6}>
                    <Card>
                        <Statistic
                            title={t('portalDemo.stats.assets')}
                            value={286}
                            suffix={t('portalDemo.stats.assetsSuffix')}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} xl={6}>
                    <Card>
                        <Statistic
                            title={t('portalDemo.stats.tasks')}
                            value={42}
                            suffix={t('portalDemo.stats.tasksSuffix')}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} xl={6}>
                    <Card>
                        <Statistic
                            title={t('portalDemo.stats.quality')}
                            value={97.8}
                            suffix="%"
                        />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                <Col xs={24} xl={12}>
                    <Card title={t('portalDemo.timeline.title')}>
                        <Timeline
                            items={[
                                {
                                    children: t(
                                        'portalDemo.timeline.item.ingest',
                                    ),
                                },
                                {
                                    children: t(
                                        'portalDemo.timeline.item.scan',
                                    ),
                                },
                                {
                                    children: t(
                                        'portalDemo.timeline.item.publish',
                                    ),
                                },
                            ]}
                        />
                    </Card>
                </Col>
                <Col xs={24} xl={12}>
                    <Card title={t('portalDemo.list.title')}>
                        <List
                            dataSource={highlights}
                            renderItem={(item) => <List.Item>{item}</List.Item>}
                        />
                    </Card>
                </Col>
            </Row>

            <Card title={t('portalDemo.table.title')}>
                <Table
                    rowKey="key"
                    pagination={false}
                    columns={columns}
                    dataSource={metricRows}
                />
            </Card>
        </div>
    );
};

export default PortalDemoPage;
