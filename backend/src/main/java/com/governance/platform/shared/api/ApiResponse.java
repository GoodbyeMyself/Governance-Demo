package com.governance.platform.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "统一接口响应体")
public class ApiResponse<T> {
    @Schema(description = "接口是否执行成功", example = "true")
    private final boolean success;

    @Schema(description = "提示信息", example = "操作成功")
    private final String message;

    @Schema(description = "业务数据")
    private final T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}



