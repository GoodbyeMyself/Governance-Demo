package com.governance.platform.modules.authcenter.service;

import com.governance.platform.modules.authcenter.entity.AuthCenterUser;
import com.governance.platform.modules.authcenter.entity.AuthCenterUserStatus;
import com.governance.platform.modules.authcenter.repository.AuthCenterUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthCenterUserDetailsService implements UserDetailsService {

    private final AuthCenterUserRepository authCenterUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        AuthCenterUser user = authCenterUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean enabled = user.getStatus() == AuthCenterUserStatus.ENABLED;
        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!enabled)
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
