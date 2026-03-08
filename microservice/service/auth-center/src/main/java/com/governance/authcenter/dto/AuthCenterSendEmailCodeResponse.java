package com.governance.authcenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送邮箱验证码响应对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送邮箱验证码响应对象")
public class AuthCenterSendEmailCodeResponse {

    /**
     * 验证码有效时长，单位为秒。
     */
    @Schema(description = "验证码有效时长（秒）", example = "300")
    private long expiresIn;

    /**
     * 重新发送等待时长，单位为秒。
     */
    @Schema(description = "重新发送等待时长（秒）", example = "60")
    private long resendIn;

    /**
     * 开发调试验证码。
     *
     * <p>仅在 mock 邮件模式下返回，方便本地演示环境联调。</p>
     */
    @Schema(description = "调试验证码，仅 mock 邮件模式返回", example = "123456")
    private String debugCode;
}
