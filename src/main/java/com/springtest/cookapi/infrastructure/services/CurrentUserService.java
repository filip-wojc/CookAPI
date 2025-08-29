package com.springtest.cookapi.infrastructure.services;

import com.springtest.cookapi.domain.exceptions.UnauthorizedException;
import com.springtest.cookapi.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private CustomUserDetails getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new UnauthorizedException("Invalid user principal type");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
