declare module '@umijs/max' {
  export function request<T = any>(
    url: string,
    options?: Record<string, any>,
  ): Promise<T>;
}
