import enUS from './locales/en-US';
import zhCN from './locales/zh-CN';
import type { LocaleCode, LocaleMessages } from './types';

export const messages: Record<LocaleCode, LocaleMessages> = {
    'zh-CN': zhCN,
    'en-US': enUS,
};
