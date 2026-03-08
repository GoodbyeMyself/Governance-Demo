package com.governance.authcenter.integration.user;

import com.governance.authcenter.integration.user.dto.InternalUserCredentialResponse;
import com.governance.authcenter.integration.user.dto.InternalUserProfileResponse;
import com.governance.authcenter.integration.user.dto.InternalUserRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "${integration.user-service.name:bms-service}",
        path = "${integration.user-service.path:/internal/users}"
)
public interface UserServiceClient {

    @PostMapping("/register")
    InternalUserProfileResponse register(@RequestBody InternalUserRegisterRequest request);

    @GetMapping("/by-username")
    InternalUserCredentialResponse getByUsername(@RequestParam("username") String username);

    @PutMapping("/{id}/last-login")
    void markLastLogin(@PathVariable("id") Long userId);
}
