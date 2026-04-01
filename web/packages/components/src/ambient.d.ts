declare module '@umijs/max' {
  export const FormattedMessage: any;
  export const Helmet: any;
  export const Link: any;
  export const SelectLang: any;
  export const history: {
    push: (...args: any[]) => void;
  };

  export function useIntl(): {
    formatMessage: (...args: any[]) => string;
  };

  export function useModel(...args: any[]): any;
  export function useRequest<TData = any, TParams extends any[] = any[]>(
    service?: (...args: TParams) => Promise<TData> | TData,
    options?: {
      manual?: boolean;
      onSuccess?: (data: any, params: any[]) => void;
      [key: string]: any;
    },
  ): any;
  export function request<T = any>(
    url: string,
    options?: Record<string, any>,
  ): Promise<T>;
  export function useSearchParams(): [URLSearchParams, any];
}

declare module 'dayjs' {
  const dayjs: any;
  export default dayjs;
}

declare module 'dayjs/plugin/relativeTime' {
  const plugin: any;
  export default plugin;
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
