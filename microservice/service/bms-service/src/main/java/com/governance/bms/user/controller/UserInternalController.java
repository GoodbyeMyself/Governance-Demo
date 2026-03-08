package com.governance.bms.user.controller;

import com.governance.bms.user.dto.UserCredentialResponse;
import com.governance.bms.user.dto.UserProfileResponse;
import com.governance.bms.user.dto.UserRegisterRequest;
import com.governance.bms.user.dto.UserRoleUpdateRequest;
import com.governance.bms.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Hidden
public class UserInternalController {

    private final UserManagementService userManagementService;

    @PostMapping("/register")
    public UserProfileResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userManagementService.register(request);
    }

    @GetMapping("/by-username")
    public UserCredentialResponse getByUsername(@RequestParam("username") String username) {
        return userManagementService.getByUsername(username);
    }

    @PutMapping("/{id}/last-login")
    public void markLastLogin(@PathVariable("id") Long userId) {
        userManagementService.markLastLogin(userId);
    }

    @GetMapping("/all")
    public List<UserProfileResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PutMapping("/{id}/role")
    public UserProfileResponse updateUserRole(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return userManagementService.updateUserRole(userId, request);
    }
}
