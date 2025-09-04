package com.springtest.cookapi.integration;

import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
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
            User user;
            switch (username) {
                case "test_user":
                    user = new User("test", "test_user", "test_password", Role.USER);
                    user.setId(1L);
                    break;
                case "test_user_2":
                    user = new User("test_2", "test_user_2", "test_password", Role.USER);
                    user.setId(2L);
                    break;
                default:
                    throw new NotFoundException("User with given username not found");
            }

            return new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
        };
    }
}
