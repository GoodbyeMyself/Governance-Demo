import { Card, Col, Row, Statistic, Typography } from 'antd';
import React from 'react';

const { Title, Paragraph } = Typography;

const ScreenHome: React.FC = () => {
  return (
    <div style={{ minHeight: '100vh', background: '#0b1220', padding: 24 }}>
      <div style={{ maxWidth: 1440, margin: '0 auto' }}>
        <Title level={2} style={{ color: '#fff', marginTop: 0 }}>
          大屏端（Screen）
        </Title>
        <Paragraph style={{ color: 'rgba(255,255,255,0.65)' }}>
          当前为 monorepo 结构下的 screen 应用骨架（Umi Max）。
        </Paragraph>
        <Row gutter={[16, 16]}>
          <Col span={8}>
            <Card>
              <Statistic title="接入数据源" value={128} />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic title="活跃任务" value={36} />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic title="告警数量" value={7} />
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default ScreenHome;
