package com.governance.shared.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息解析器。
 *
 * <p>业务代码通过该组件按当前请求语言快速获取本地化文本，
 * 避免在控制器、服务和异常处理中重复编写 {@link MessageSource} 调用逻辑。</p>
 */
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessageSource messageSource;

    /**
     * 按当前请求语言解析消息。
     *
     * @param code 消息编码
     * @param args 占位参数
     * @return 本地化后的消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(LocaleContextHolder.getLocale(), code, args);
    }

    /**
     * 按指定语言解析消息。
     *
     * @param locale 目标语言
     * @param code   消息编码
     * @param args   占位参数
     * @return 本地化后的消息
     */
    public String getMessage(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
