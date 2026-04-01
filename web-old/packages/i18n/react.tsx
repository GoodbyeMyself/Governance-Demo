import { ConfigProvider } from 'antd';
import { createContext, useContext, useMemo, useSyncExternalStore } from 'react';
import { antdLocaleMap, getLocale, setLocale, subscribeLocale, translate } from './store';
import type { LocaleCode, TranslationParams } from './types';

export interface I18nContextValue {
    locale: LocaleCode;
    setLocale: (locale: LocaleCode) => void;
    t: (key: string, params?: TranslationParams) => string;
}

const I18nContext = createContext<I18nContextValue | null>(null);

const useLocaleSnapshot = () =>
    useSyncExternalStore(subscribeLocale, getLocale, getLocale);

export const I18nProvider: React.FC<React.PropsWithChildren> = ({
    children,
}) => {
    const locale = useLocaleSnapshot();
    const contextValue = useMemo<I18nContextValue>(
        () => ({
            locale,
            setLocale,
            t: (key, params) => translate(key, params),
        }),
        [locale],
    );

    return (
        <I18nContext.Provider value={contextValue}>
            <ConfigProvider locale={antdLocaleMap[locale]}>
                {children}
            </ConfigProvider>
        </I18nContext.Provider>
    );
};

export const useI18n = () => {
    const context = useContext(I18nContext);
    if (!context) {
        throw new Error('useI18n must be used within I18nProvider');
    }

    return context;
};
