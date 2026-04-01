import { I18nProvider } from '@governance/i18n';
import { configureHttpClient } from '@governance/utils';
import { message } from 'antd';
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './global.less';

configureHttpClient({
    clientAppName: 'portal',
    onUnauthorized: (messageText) => {
        message.warning(messageText);
    },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <I18nProvider>
            <BrowserRouter>
                <App />
            </BrowserRouter>
        </I18nProvider>
    </React.StrictMode>,
);
