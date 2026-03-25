import { Dropdown } from 'antd';
import type { MenuProps } from 'antd';
import { SelectLang as UmiSelectLang } from '@umijs/max';
import IconFont from '../IconFont';

export type SiderTheme = 'light' | 'realDark';

type QuestionProps = {
  navTheme?: SiderTheme;
  onThemeChange?: (theme: SiderTheme) => void;
};

export const SelectLang: React.FC = () => {
  return (
    <UmiSelectLang
      style={{
        padding: 4,
      }}
    />
  );
};

export const Question: React.FC<QuestionProps> = ({
  navTheme = 'light',
  onThemeChange,
}) => {
  const menu: MenuProps = {
    selectable: true,
    selectedKeys: [navTheme],
    items: [
      {
        key: 'light',
        label: '浅色模式',
      },
      {
        key: 'realDark',
        label: '暗黑模式',
      },
    ],
    onClick: ({ key }) => {
      onThemeChange?.(key as SiderTheme);
    },
  };

  return (
    <Dropdown menu={menu} placement="bottomRight" trigger={['hover']}>
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          padding: 6,
          cursor: 'pointer',
        }}
      >
        <IconFont type="icon-ziyuan1" size={16} />
      </span>
    </Dropdown>
  );
};
