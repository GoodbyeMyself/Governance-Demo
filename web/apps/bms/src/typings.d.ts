declare module 'slash2';
declare module '*.css';
declare module '*.less';
declare module '*.scss';
declare module '*.sass';
declare module '*.svg';
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.bmp';
declare module '*.tiff';
declare module 'omit.js';
declare module 'numeral';
declare module 'dayjs';
declare module 'dayjs/plugin/relativeTime';
declare module 'mockjs';
declare module 'react-fittext';
declare module 'antd/es/dropdown' {
  export type DropdownProps = any;
  export type DropDownProps = any;
}
declare module 'antd/es/form/interface' {
  export type Store = Record<string, any>;
}
declare module 'react/jsx-runtime' {
  export const Fragment: any;
  export function jsx(type: any, props: any, key?: any): any;
  export function jsxs(type: any, props: any, key?: any): any;
}
declare namespace JSX {
  interface IntrinsicAttributes {
    key?: any;
  }
  interface IntrinsicElements {
    [elemName: string]: any;
  }
}

declare const REACT_APP_ENV: 'test' | 'dev' | 'pre' | false;
