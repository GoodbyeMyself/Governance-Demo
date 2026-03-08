package com.governance.authcenter.integration.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户中心内部密码重置请求对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserPasswordResetRequest {

    /**
     * 新密码。
     */
    private String newPassword;
}
