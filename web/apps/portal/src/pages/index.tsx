import { Card, Typography } from 'antd';
import React from 'react';

const { Title, Paragraph } = Typography;

const PortalHome: React.FC = () => {
  return (
    <div style={{ maxWidth: 960, margin: '40px auto', padding: '0 16px' }}>
      <Card>
        <Title level={2} style={{ marginTop: 0 }}>
          门户端（Portal）
        </Title>
        <Paragraph>
          当前为 monorepo 结构下的 portal 应用骨架（Umi Max）。
        </Paragraph>
        <Paragraph>下一步将逐步接入公共包与页面模块。</Paragraph>
      </Card>
    </div>
  );
};

export default PortalHome;
