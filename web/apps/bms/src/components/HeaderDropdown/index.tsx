import { Dropdown } from 'antd';
import type { DropdownProps } from 'antd/es/dropdown';
import { createStyles } from 'antd-style';
import classNames from 'classnames';
import React from 'react';

const useStyles = createStyles(({ token }) => {
  return {
    dropdown: {
      [`@media screen and (max-width: ${token.screenXS}px)`]: {
        width: '100%',
      },
    },
  };
});

export type HeaderDropdownProps = {
  overlayClassName?: string;
  placement?:
    | 'bottomLeft'
    | 'bottomRight'
    | 'topLeft'
    | 'topCenter'
    | 'topRight'
    | 'bottomCenter';
} & Omit<DropdownProps, 'overlay'>;

const HeaderDropdown: React.FC<HeaderDropdownProps> = ({
  overlayClassName: cls,
  ...restProps
}) => {
  const { styles } = useStyles();
  return (
    <Dropdown
      overlayClassName={classNames(styles.dropdown, cls)}
      {...restProps}
    />
  );
};

export default HeaderDropdown;
