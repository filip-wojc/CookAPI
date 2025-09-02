package com.springtest.cookapi.integration;

import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.infrastructure.security.CustomUserDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return username -> {
            // Stwórz obiekt typu, jakiego oczekuje CurrentUserService
            User user = new User("test", "test_user", "test_password", Role.USER);
            user.setId(1L);

            return new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
        };
    }
}
