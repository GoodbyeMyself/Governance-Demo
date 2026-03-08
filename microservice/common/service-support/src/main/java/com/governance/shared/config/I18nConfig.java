package com.governance.shared.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * 后端国际化配置。
 *
 * <p>该配置统一完成三件事情：
 * <ul>
 *     <li>根据请求头 {@code Accept-Language} 识别当前语言环境</li>
 *     <li>加载公共消息资源与各服务自己的消息资源</li>
 *     <li>让 Bean Validation 的校验错误也走同一套国际化消息源</li>
 * </ul>
 * </p>
 */
@Configuration
public class I18nConfig {

    /**
     * 注册基于请求头的语言解析器。
     *
     * <p>当前平台先支持中文与英文两种语言，默认使用中文。</p>
     *
     * @return 语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        localeResolver.setSupportedLocales(
                List.of(
                        Locale.SIMPLIFIED_CHINESE,
                        Locale.CHINA,
                        Locale.ENGLISH,
                        Locale.US
                )
        );
        return localeResolver;
    }

    /**
     * 注册统一消息源。
     *
     * <p>公共消息放在 {@code i18n/common/messages}，
     * 各服务自己的消息放在 {@code i18n/messages}。</p>
     *
     * @return Spring 消息源
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:i18n/common/messages",
                "classpath:i18n/messages"
        );
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 注册校验器，使 Bean Validation 的错误信息也能按当前语言返回。
     *
     * @param messageSource 国际化消息源
     * @return 校验器
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
