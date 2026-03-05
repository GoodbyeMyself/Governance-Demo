import { CustomerServiceOutlined } from '@ant-design/icons';
import {
    type MouseEvent as ReactMouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
    type FC,
} from 'react';
import styles from './index.less';

const DEFAULT_PANEL_WIDTH = 520;
const MIN_PANEL_WIDTH = 260;
const MOBILE_BREAKPOINT = 768;
const VIEWPORT_HORIZONTAL_GAP = 24;

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

const getMaxPanelWidth = (viewportWidth: number) => {
    return Math.max(MIN_PANEL_WIDTH, viewportWidth - VIEWPORT_HORIZONTAL_GAP);
};

const clampWidth = (width: number, viewportWidth: number) => {
    const maxWidth = getMaxPanelWidth(viewportWidth);
    return Math.min(Math.max(width, MIN_PANEL_WIDTH), maxWidth);
};

type ResizeListeners = {
    onMouseMove: (event: MouseEvent) => void;
    onMouseUp: () => void;
};

const GovernanceAssistant: FC = () => {
    const [visible, setVisible] = useState(false);
    const [isResizing, setIsResizing] = useState(false);
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

    const assistantConfig = useMemo(() => getAssistantConfig(), []);
    const hasIframeUrl = Boolean(assistantConfig.iframeUrl);
    const isMobile = viewportWidth <= MOBILE_BREAKPOINT;

    const clearResizeListeners = useCallback(() => {
        const listeners = resizeListenersRef.current;
        const hasResizeFrame = resizeFrameRef.current !== null;
        if (!listeners && !hasResizeFrame && !isResizingRef.current) return;

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
            if (isMobile || event.button !== 0) return;

            event.preventDefault();
            clearResizeListeners();
            isResizingRef.current = true;
            setIsResizing(true);

            const startX = event.clientX;
            const startWidth = panelWidth;
            const onMouseMove = (moveEvent: MouseEvent) => {
                if (!isResizingRef.current) return;
                if (moveEvent.buttons === 0) {
                    clearResizeListeners();
                    return;
                }

                const delta = startX - moveEvent.clientX;
                const nextWidth = clampWidth(
                    startWidth + delta,
                    window.innerWidth,
                );
                pendingWidthRef.current = nextWidth;
                if (resizeFrameRef.current !== null) return;

                resizeFrameRef.current = window.requestAnimationFrame(() => {
                    resizeFrameRef.current = null;
                    if (pendingWidthRef.current !== null) {
                        setPanelWidth(pendingWidthRef.current);
                    }
                });
            };
            const onMouseUp = () => {
                clearResizeListeners();
            };

            resizeListenersRef.current = { onMouseMove, onMouseUp };

            document.body.style.userSelect = 'none';
            document.body.style.cursor = 'ew-resize';
            window.addEventListener('mousemove', onMouseMove);
            window.addEventListener('mouseup', onMouseUp);
            window.addEventListener('mouseleave', onMouseUp);
        },
        [clearResizeListeners, isMobile, panelWidth],
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

    const panelStyle = isMobile
        ? undefined
        : { width: `${clampWidth(panelWidth, viewportWidth)}px` };

    return (
        <div className={styles.wrapper}>
            <div
                className={`${styles.panel} ${visible ? styles.panelVisible : ''} ${isResizing ? styles.panelResizing : ''}`}
                style={panelStyle}
            >
                <div
                    className={`${styles.resizeHandle} ${isResizing ? styles.resizeHandleActive : ''}`}
                    onMouseDown={handleResizeStart}
                />
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
