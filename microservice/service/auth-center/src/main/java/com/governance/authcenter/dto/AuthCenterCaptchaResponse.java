package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图形验证码响应对象")
public class AuthCenterCaptchaResponse {

    /**
     * 验证码 ID。
     */
    @Schema(description = "验证码 ID")
    private String captchaId;

    /**
     * Base64 Data URL 格式的验证码图片。
     */
    @Schema(description = "验证码图片 Data URL")
    private String imageData;

    /**
     * 过期时长，单位为秒。
     */
    @Schema(description = "过期时长（秒）", example = "180")
    private long expiresIn;
}
