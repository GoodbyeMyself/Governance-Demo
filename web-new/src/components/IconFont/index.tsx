import AntdIcon from '@ant-design/icons';
import type { CSSProperties, ComponentProps, SVGProps } from 'react';

type AntdIconProps = Omit<ComponentProps<typeof AntdIcon>, 'component' | 'viewBox'>;

export type IconFontProps = AntdIconProps & {
  type: string;
  size?: number | string;
  color?: string;
  title?: string;
};

const formatSize = (size: number | string) => {
  return typeof size === 'number' ? `${size}px` : size;
};

const createIconComponent = (type: string, title?: string) => {
  const IconComponent = (props: SVGProps<SVGSVGElement>) => {
    return (
      <svg
        {...props}
        aria-hidden={title ? undefined : true}
        fill="currentColor"
        focusable="false"
        height="1em"
        viewBox="0 0 1024 1024"
        width="1em"
      >
        {title ? <title>{title}</title> : null}
        <use xlinkHref={`#${type}`} />
      </svg>
    );
  };

  return IconComponent;
};

const IconFont: React.FC<IconFontProps> = ({
  type,
  size,
  color,
  title,
  style,
  ...restProps
}) => {
  const mergedStyle: CSSProperties = {
    ...style,
    ...(size ? { fontSize: formatSize(size) } : {}),
    ...(color ? { color } : {}),
  };

  return (
    <AntdIcon
      {...restProps}
      component={createIconComponent(type, title)}
      style={mergedStyle}
      title={title}
    />
  );
};

export default IconFont;
