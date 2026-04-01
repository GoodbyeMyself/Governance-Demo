import type { Locale } from 'antd/es/locale';
import enUS from 'antd/locale/en_US';
import zhCN from 'antd/locale/zh_CN';
import { messages } from './messages';
import type { LocaleCode, TranslationParams } from './types';

export const DEFAULT_LOCALE: LocaleCode = 'zh-CN';
export const LANGUAGE_STORAGE_KEY = 'governance-locale';

const listeners = new Set<() => void>();

const hasWindow = () => typeof window !== 'undefined';

const normalizeLocale = (locale?: string | null): LocaleCode =>
    locale === 'en-US' ? 'en-US' : 'zh-CN';

const readStoredLocale = (): LocaleCode => {
    if (!hasWindow()) {
        return DEFAULT_LOCALE;
    }

    return normalizeLocale(window.localStorage.getItem(LANGUAGE_STORAGE_KEY));
};

let currentLocale: LocaleCode = readStoredLocale();

const notifyLocaleChange = () => {
    listeners.forEach((listener) => listener());
};

const syncDocumentLang = (locale: LocaleCode) => {
    if (!hasWindow()) {
        return;
    }

    document.documentElement.lang = locale;
};

syncDocumentLang(currentLocale);

const interpolate = (template: string, params?: TranslationParams) => {
    if (!params) {
        return template;
    }

    return template.replace(/\{(\w+)\}/g, (_, key: string) => {
        const value = params[key];
        return value === undefined || value === null ? '' : String(value);
    });
};

export const getLocale = (): LocaleCode => currentLocale;

export const setLocale = (locale: LocaleCode) => {
    const nextLocale = normalizeLocale(locale);
    if (nextLocale === currentLocale) {
        return;
    }

    currentLocale = nextLocale;
    if (hasWindow()) {
        window.localStorage.setItem(LANGUAGE_STORAGE_KEY, nextLocale);
    }
    syncDocumentLang(nextLocale);
    notifyLocaleChange();
};

export const subscribeLocale = (listener: () => void) => {
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
};

export const translate = (key: string, params?: TranslationParams): string => {
    const localeMessages = messages[getLocale()];
    const defaultMessages = messages[DEFAULT_LOCALE];
    const template = localeMessages[key] || defaultMessages[key] || key;
    return interpolate(template, params);
};

export const antdLocaleMap: Record<LocaleCode, Locale> = {
    'zh-CN': zhCN,
    'en-US': enUS,
};
