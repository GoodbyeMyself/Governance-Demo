import { Button, Result } from 'antd';
import React from 'react';

export type NotFoundProps = {
  homeHref?: string;
  homeText?: string;
  subTitle?: string;
};

const NotFound: React.FC<NotFoundProps> = ({
  homeHref = '/',
  homeText = '返回首页',
  subTitle = '页面不存在',
}) => {
  return (
    <Result
      status="404"
      title="404"
      subTitle={subTitle}
      extra={
        <Button type="primary" href={homeHref}>
          {homeText}
        </Button>
      }
    />
  );
};

export default NotFound;
