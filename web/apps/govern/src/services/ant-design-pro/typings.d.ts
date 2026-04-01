// @ts-ignore
/* eslint-disable */

declare namespace API {
  type BackendApiResponse<T> = {
    success: boolean;
    message: string;
    data: T;
  };

  type CurrentUser = {
    id?: number;
    username?: string;
    nickname?: string;
    name?: string;
    avatar?: string;
    userid?: string;
    email?: string;
    phone?: string;
    role?: 'USER' | 'ADMIN';
    status?: 'ENABLED' | 'DISABLED';
    lastLoginAt?: string;
    createdAt?: string;
    access?: 'user' | 'admin';
  };

  type LoginResult = {
    success?: boolean;
    message?: string;
    status?: string;
    type?: string;
    currentAuthority?: string;
    data?: {
      expiresIn?: number;
      user?: CurrentUser;
    };
  };

  type CaptchaResult = {
    captchaId?: string;
    imageData?: string;
    expiresIn?: number;
  };

  type PageParams = {
    current?: number;
    pageSize?: number;
  };

  type RuleListItem = {
    key?: number;
    disabled?: boolean;
    href?: string;
    avatar?: string;
    name?: string;
    owner?: string;
    desc?: string;
    callNo?: number;
    status?: number;
    updatedAt?: string;
    createdAt?: string;
    progress?: number;
  };

  type RuleList = {
    data?: RuleListItem[];
    /** 列表的内容总数 */
    total?: number;
    success?: boolean;
  };

  type FakeCaptcha = {
    captchaId?: string;
    imageData?: string;
    expiresIn?: number;
  };

  type LoginParams = {
    username?: string;
    password?: string;
    captchaId?: string;
    captchaCode?: string;
    autoLogin?: boolean;
    type?: string;
  };

  type ErrorResponse = {
    /** 业务约定的错误码 */
    errorCode: string;
    /** 业务上的错误信息 */
    errorMessage?: string;
    /** 业务上的请求是否成功 */
    success?: boolean;
  };

  type NoticeIconList = {
    data?: NoticeIconItem[];
    /** 列表的内容总数 */
    total?: number;
    success?: boolean;
  };

  type NoticeIconItemType = 'notification' | 'message' | 'event';

  type NoticeIconItem = {
    id?: string;
    extra?: string;
    key?: string;
    read?: boolean;
    avatar?: string;
    title?: string;
    status?: string;
    datetime?: string;
    description?: string;
    type?: NoticeIconItemType;
  };
}
