import { CustomerServiceOutlined } from '@ant-design/icons';
import { useI18n } from '@governance/i18n';
import { Spin } from 'antd';
import {
    type FC,
    type MouseEvent as ReactMouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import styles from './index.module.less';

type AssistantRuntimeConfig = {
    assistant?: {
        title?: string;
        iframeUrl?: string;
    };
};

type ResizeListeners = {
    onMouseMove: (event: MouseEvent) => void;
    onMouseUp: () => void;
};

const DEFAULT_PANEL_WIDTH = 520;
const MIN_PANEL_WIDTH = 260;
const MOBILE_BREAKPOINT = 768;
const VIEWPORT_HORIZONTAL_GAP = 24;

const getAssistantConfig = (fallbackTitle: string) => {
    if (typeof window === 'undefined') {
        return {
            title: fallbackTitle,
            iframeUrl: '',
        };
    }

    const runtimeConfig = (
        window as Window & {
            __DATA_GOVERNANCE_CONFIG__?: AssistantRuntimeConfig;
        }
    ).__DATA_GOVERNANCE_CONFIG__;
    const assistant = runtimeConfig?.assistant;

    return {
        title: assistant?.title?.trim() || fallbackTitle,
        iframeUrl: assistant?.iframeUrl?.trim() || '',
    };
};

const getMaxPanelWidth = (viewportWidth: number) =>
    Math.max(MIN_PANEL_WIDTH, viewportWidth - VIEWPORT_HORIZONTAL_GAP);

const clampWidth = (width: number, viewportWidth: number) => {
    const maxWidth = getMaxPanelWidth(viewportWidth);
    return Math.min(Math.max(width, MIN_PANEL_WIDTH), maxWidth);
};

const GovernanceAssistant: FC = () => {
    const { t } = useI18n();
    const [visible, setVisible] = useState(false);
    const [isResizing, setIsResizing] = useState(false);
    const [iframeLoading, setIframeLoading] = useState(false);
    const [iframeError, setIframeError] = useState(false);
    const [iframeReloadKey, setIframeReloadKey] = useState(0);
    const [viewportWidth, setViewportWidth] = useState(() =>
        typeof window === 'undefined' ? 1920 : window.innerWidth,
    );
    const [panelWidth, setPanelWidth] = useState(() =>
        clampWidth(DEFAULT_PANEL_WIDTH, viewportWidth),
    );

    const resizeListenersRef = useRef<ResizeListeners | null>(null);
    const resizeFrameRef = useRef<number | null>(null);
    const pendingWidthRef = useRef<number | null>(null);
    const isResizingRef = useRef(false);

    const assistantConfig = useMemo(
        () => getAssistantConfig(t('assistant.defaultTitle')),
        [t],
    );
    const hasIframeUrl = Boolean(assistantConfig.iframeUrl);
    const isMobile = viewportWidth <= MOBILE_BREAKPOINT;

    const clearResizeListeners = useCallback(() => {
        const listeners = resizeListenersRef.current;
        const hasResizeFrame = resizeFrameRef.current !== null;
        if (!listeners && !hasResizeFrame && !isResizingRef.current) {
            return;
        }

        if (listeners) {
            window.removeEventListener('mousemove', listeners.onMouseMove);
            window.removeEventListener('mouseup', listeners.onMouseUp);
            window.removeEventListener('mouseleave', listeners.onMouseUp);
        }

        if (resizeFrameRef.current !== null) {
            window.cancelAnimationFrame(resizeFrameRef.current);
            resizeFrameRef.current = null;
        }

        pendingWidthRef.current = null;
        document.body.style.userSelect = '';
        document.body.style.cursor = '';
        resizeListenersRef.current = null;
        isResizingRef.current = false;
        setIsResizing(false);
    }, []);

    const handleResizeStart = useCallback(
        (event: ReactMouseEvent<HTMLDivElement>) => {
            if (isMobile || event.button !== 0) {
                return;
            }

            event.preventDefault();
            clearResizeListeners();
            isResizingRef.current = true;
            setIsResizing(true);

            const startX = event.clientX;
            const startWidth = panelWidth;

            const flushPendingWidth = () => {
                resizeFrameRef.current = null;
                const nextWidth = pendingWidthRef.current;
                pendingWidthRef.current = null;
                if (nextWidth !== null) {
                    setPanelWidth(nextWidth);
                }
            };

            const onMouseMove = (moveEvent: MouseEvent) => {
                if (!isResizingRef.current) {
                    return;
                }

                if (moveEvent.buttons === 0) {
                    clearResizeListeners();
                    return;
                }

                const delta = startX - moveEvent.clientX;
                const nextWidth = clampWidth(startWidth + delta, viewportWidth);
                pendingWidthRef.current = nextWidth;

                if (resizeFrameRef.current === null) {
                    resizeFrameRef.current =
                        window.requestAnimationFrame(flushPendingWidth);
                }
            };

            const onMouseUp = () => {
                clearResizeListeners();
            };

            resizeListenersRef.current = {
                onMouseMove,
                onMouseUp,
            };

            document.body.style.userSelect = 'none';
            document.body.style.cursor = 'col-resize';
            window.addEventListener('mousemove', onMouseMove);
            window.addEventListener('mouseup', onMouseUp);
            window.addEventListener('mouseleave', onMouseUp);
        },
        [clearResizeListeners, isMobile, panelWidth, viewportWidth],
    );

    useEffect(() => {
        const handleWindowResize = () => {
            const nextViewportWidth = window.innerWidth;
            setViewportWidth(nextViewportWidth);
            setPanelWidth((prev) => clampWidth(prev, nextViewportWidth));
        };

        window.addEventListener('resize', handleWindowResize);
        return () => {
            window.removeEventListener('resize', handleWindowResize);
        };
    }, []);

    useEffect(() => {
        const handleWindowBlur = () => {
            clearResizeListeners();
        };

        window.addEventListener('blur', handleWindowBlur);
        return () => {
            window.removeEventListener('blur', handleWindowBlur);
        };
    }, [clearResizeListeners]);

    useEffect(() => {
        return () => {
            clearResizeListeners();
        };
    }, [clearResizeListeners]);

    useEffect(() => {
        if (!visible) {
            clearResizeListeners();
        }
    }, [clearResizeListeners, visible]);

    useEffect(() => {
        if (visible && hasIframeUrl) {
            setIframeLoading(true);
            setIframeError(false);
        }
    }, [hasIframeUrl, iframeReloadKey, visible]);

    const panelStyle = isMobile
        ? undefined
        : { width: `${clampWidth(panelWidth, viewportWidth)}px` };

    const handleIframeLoad = () => {
        setIframeLoading(false);
        setIframeError(false);
    };

    const handleIframeError = () => {
        setIframeLoading(false);
        setIframeError(true);
    };

    const reloadIframe = () => {
        setIframeLoading(true);
        setIframeError(false);
        setIframeReloadKey((prev) => prev + 1);
    };

    return (
        <div className={styles.wrapper}>
            {visible ? (
                <div
                    className={`${styles.panel} ${styles.panelVisible} ${
                        isResizing ? styles.panelResizing : ''
                    }`}
                    style={panelStyle}
                >
                    <div
                        className={`${styles.resizeHandle} ${
                            isResizing ? styles.resizeHandleActive : ''
                        }`}
                        onMouseDown={handleResizeStart}
                    />

                    <div className={styles.header}>
                        <span className={styles.title}>
                            {assistantConfig.title}
                        </span>
                        <button
                            type="button"
                            className={styles.closeButton}
                            onClick={() => setVisible(false)}
                        >
                            {t('assistant.close')}
                        </button>
                    </div>

                    <div className={styles.body}>
                        {hasIframeUrl ? (
                            <div className={styles.iframeWrap}>
                                <iframe
                                    key={iframeReloadKey}
                                    className={`${styles.iframe} ${
                                        iframeLoading
                                            ? styles.iframeLoading
                                            : ''
                                    }`}
                                    src={assistantConfig.iframeUrl}
                                    title={assistantConfig.title}
                                    onLoad={handleIframeLoad}
                                    onError={handleIframeError}
                                />

                                {iframeLoading ? (
                                    <div className={styles.loadingMask}>
                                        <Spin size="large" />
                                        <div className={styles.loadingText}>
                                            {t('assistant.loading')}
                                        </div>
                                    </div>
                                ) : null}

                                {iframeError && !iframeLoading ? (
                                    <div className={styles.errorMask}>
                                        <div className={styles.errorText}>
                                            {t('assistant.loadFailed')}
                                        </div>
                                        <button
                                            type="button"
                                            className={styles.retryButton}
                                            onClick={reloadIframe}
                                        >
                                            {t('assistant.reload')}
                                        </button>
                                    </div>
                                ) : null}
                            </div>
                        ) : (
                            <div className={styles.empty}>
                                {t('assistant.missingUrl')}
                            </div>
                        )}
                    </div>
                </div>
            ) : null}

            {!visible ? (
                <button
                    type="button"
                    className={styles.trigger}
                    onClick={() => setVisible(true)}
                >
                    <CustomerServiceOutlined />
                    <span>{t('assistant.trigger')}</span>
                </button>
            ) : null}
        </div>
    );
};

export default GovernanceAssistant;
