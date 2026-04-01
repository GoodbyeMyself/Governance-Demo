import type { RequestOptions } from '@@/plugin-request/request';
import type { RequestConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { message, notification } from 'antd';

enum ErrorShowType {
  SILENT = 0,
  WARN_MESSAGE = 1,
  ERROR_MESSAGE = 2,
  NOTIFICATION = 3,
  REDIRECT = 9,
}

interface ResponseStructure {
  success?: boolean;
  data?: unknown;
  message?: string;
  errorCode?: number;
  errorMessage?: string;
  showType?: ErrorShowType;
}

const loginPath = '/user/login';
const authFreePaths = [loginPath, '/user/register', '/user/register-result'];

function redirectToLogin() {
  const { pathname, search } = history.location;

  if (authFreePaths.includes(pathname)) {
    return;
  }

  const redirect = pathname + search;
  history.replace({
    pathname: loginPath,
    search: redirect ? `?redirect=${encodeURIComponent(redirect)}` : undefined,
  });
}

export const errorConfig: RequestConfig = {
  errorConfig: {
    errorThrower: (res) => {
      const { success, data, message: responseMessage, errorCode, errorMessage, showType } =
        res as ResponseStructure;

      if (success === false) {
        const error: any = new Error(errorMessage || responseMessage || 'Request failed');
        error.name = 'BizError';
        error.info = {
          errorCode,
          errorMessage: errorMessage || responseMessage,
          showType,
          data,
        };
        throw error;
      }
    },
    errorHandler: (error: any, opts: any) => {
      if (opts?.skipErrorHandler) {
        throw error;
      }

      if (error?.response?.status === 401) {
        redirectToLogin();
        return;
      }

      if (error.name === 'BizError') {
        const errorInfo = error.info as ResponseStructure | undefined;

        if (errorInfo) {
          const errorMessage = errorInfo.errorMessage || '请求失败';
          const errorCode = errorInfo.errorCode;

          switch (errorInfo.showType) {
            case ErrorShowType.SILENT:
              break;
            case ErrorShowType.WARN_MESSAGE:
              message.warning(errorMessage);
              break;
            case ErrorShowType.NOTIFICATION:
              notification.open({
                description: errorMessage,
                message: errorCode,
              });
              break;
            case ErrorShowType.REDIRECT:
              redirectToLogin();
              break;
            default:
              message.error(errorMessage);
          }
        }
        return;
      }

      if (error.response) {
        message.error(`Response status: ${error.response.status}`);
        return;
      }

      if (error.request) {
        message.error('None response! Please retry.');
        return;
      }

      message.error('Request error, please retry.');
    },
  },
  requestInterceptors: [
    (config: RequestOptions) => {
      return {
        ...config,
        credentials: 'include',
      };
    },
  ],
};
