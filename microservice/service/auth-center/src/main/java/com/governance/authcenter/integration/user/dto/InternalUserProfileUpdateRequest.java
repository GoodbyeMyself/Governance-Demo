package com.governance.authcenter.integration.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户中心内部资料更新请求对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserProfileUpdateRequest {

    /**
     * 用户名。
     */
    private String username;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;
}
