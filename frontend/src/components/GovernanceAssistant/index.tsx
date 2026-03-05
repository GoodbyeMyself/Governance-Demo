import { CustomerServiceOutlined } from '@ant-design/icons';
import { useMemo, useState, type FC } from 'react';
import styles from './index.less';

const getAssistantConfig = () => {
    if (typeof window === 'undefined') {
        return {
            title: '数据治理助手',
            iframeUrl: '',
        };
    }

    const assistant = window.__DATA_GOVERNANCE_CONFIG__?.assistant;
    return {
        title: assistant?.title?.trim() || '数据治理助手',
        iframeUrl: assistant?.iframeUrl?.trim() || '',
    };
};

const GovernanceAssistant: FC = () => {
    const [visible, setVisible] = useState(false);

    const assistantConfig = useMemo(() => getAssistantConfig(), []);
    const hasIframeUrl = Boolean(assistantConfig.iframeUrl);

    return (
        <div className={styles.wrapper}>
            <div
                className={`${styles.panel} ${visible ? styles.panelVisible : ''}`}
            >
                <div className={styles.header}>
                    <span className={styles.title}>{assistantConfig.title}</span>
                    <button
                        type="button"
                        className={styles.closeButton}
                        onClick={() => setVisible(false)}
                    >
                        关闭
                    </button>
                </div>
                <div className={styles.body}>
                    {hasIframeUrl ? (
                        <iframe
                            className={styles.iframe}
                            src={assistantConfig.iframeUrl}
                            title={assistantConfig.title}
                        />
                    ) : (
                        <div className={styles.empty}>
                            助手地址未配置，请修改 runtime-config.js
                        </div>
                    )}
                </div>
            </div>

            {!visible && (
                <button
                    type="button"
                    className={styles.trigger}
                    onClick={() => setVisible(true)}
                >
                    <CustomerServiceOutlined />
                    <span>治理助手</span>
                </button>
            )}
        </div>
    );
};

export default GovernanceAssistant;
