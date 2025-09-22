package com.springtest.cookapi.infrastructure.services;

import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.exceptions.UnauthorizedException;
import com.springtest.cookapi.domain.mappers.UserMapper;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
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

    public UserDto getCurrentUserDto() {
        var userDetails = getCurrentUser();
        var userObject = userRepository.findById(userDetails.getUserId()).orElse(null);
        if (userObject == null) {
            throw new NotFoundException("User not found");
        }
        return userMapper.toUserDto(userObject);
    }
}
