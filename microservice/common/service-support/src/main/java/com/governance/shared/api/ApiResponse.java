package com.governance.shared.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用接口响应包装对象。
 * <p>
 * 各微服务统一使用该对象作为 REST 返回体，
 * 通过 success、message、data 三个字段表达请求结果，
 * 便于前端和网关做统一解析。
 *
 * @param <T> 业务数据类型
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 请求是否成功。
     */
    private final boolean success;

    /**
     * 返回消息，通常用于提示成功或失败原因。
     */
    private final String message;

    /**
     * 业务数据载荷。
     */
    private final T data;

    /**
     * 构造成功响应。
     *
     * @param message 成功消息
     * @param data    业务数据
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 构造失败响应，且不携带业务数据。
     *
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * 构造失败响应，同时保留额外返回数据。
     *
     * @param message 失败消息
     * @param data    附加数据
     * @param <T>     数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
