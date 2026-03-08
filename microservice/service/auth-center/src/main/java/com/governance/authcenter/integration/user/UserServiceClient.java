package com.governance.authcenter.integration.user;

import com.governance.authcenter.integration.user.dto.InternalUserCredentialResponse;
import com.governance.authcenter.integration.user.dto.InternalUserPasswordResetRequest;
import com.governance.authcenter.integration.user.dto.InternalUserProfileResponse;
import com.governance.authcenter.integration.user.dto.InternalUserProfileUpdateRequest;
import com.governance.authcenter.integration.user.dto.InternalUserRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户中心内部调用客户端。
 *
 * <p>认证中心通过该 Feign 客户端访问基础用户服务，
 * 完成注册落库、按用户名查询凭据以及更新用户资料等操作。</p>
 */
@FeignClient(
        name = "${integration.user-service.name:bms-service}",
        path = "${integration.user-service.path:/internal/users}"
)
public interface UserServiceClient {

    /**
     * 注册新用户。
     *
     * @param request 注册请求参数
     * @return 注册后的用户资料
     */
    @PostMapping("/register")
    InternalUserProfileResponse register(@RequestBody InternalUserRegisterRequest request);

    /**
     * 按用户名查询用户凭据。
     *
     * @param username 用户名
     * @return 用户凭据对象
     */
    @GetMapping("/by-username")
    InternalUserCredentialResponse getByUsername(@RequestParam("username") String username);

    /**
     * 标记用户最近登录时间。
     *
     * @param userId 用户 ID
     */
    @PutMapping("/{id}/last-login")
    void markLastLogin(@PathVariable("id") Long userId);

    /**
     * 按用户名更新用户资料。
     *
     * @param username 当前用户名
     * @param request  用户资料更新请求
     * @return 更新后的用户资料
     */
    @PutMapping("/by-username/{username}/profile")
    InternalUserProfileResponse updateUserProfileByUsername(
            @PathVariable("username") String username,
            @RequestBody InternalUserProfileUpdateRequest request
    );

    /**
     * 按用户名重置密码。
     *
     * @param username 用户名
     * @param request  密码重置请求
     */
    @PutMapping("/by-username/{username}/password")
    void resetPasswordByUsername(
            @PathVariable("username") String username,
            @RequestBody InternalUserPasswordResetRequest request
    );
}
