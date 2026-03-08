import type { LocaleMessages } from '../../../types';

const httpMessages: LocaleMessages = {
    'http.400': '请求参数错误，请检查输入内容',
    'http.401': '登录已过期，请重新登录',
    'http.403': '暂无权限访问当前资源',
    'http.404': '请求的接口不存在',
    'http.405': '请求方法不支持',
    'http.408': '请求超时，请稍后重试',
    'http.409': '资源状态冲突，请刷新后重试',
    'http.422': '请求校验失败，请检查输入内容',
    'http.500': '服务异常，请稍后重试',
    'http.default': '请求失败，请稍后重试',
    'http.networkError': '网络异常，请检查网络连接',
    'http.requestDataRequired': '{method} 请求缺少 data 参数',
    'http.requestUrlRequired': '请求地址不能为空',
};

export default httpMessages;
