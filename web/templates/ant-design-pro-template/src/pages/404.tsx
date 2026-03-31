import { useIntl } from '@umijs/max';
import { NotFound } from '@components';
import React from 'react';

const NoFoundPage: React.FC = () => {
  const intl = useIntl();

  return (
    <NotFound
      subTitle={intl.formatMessage({ id: 'pages.404.subTitle' })}
      homeText={intl.formatMessage({ id: 'pages.404.buttonText' })}
      homeHref="/"
    />
  );
};

export default NoFoundPage;
