package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.requests.LoginRequest;
import com.springtest.cookapi.domain.requests.RefreshTokenRequest;
import com.springtest.cookapi.domain.requests.RegisterRequest;
import com.springtest.cookapi.domain.responses.TokenPair;
import com.springtest.cookapi.infrastructure.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokenPair = authService.login(request);
        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = authService.refreshToken(request);
        return ResponseEntity.ok(tokenPair);
    }


    // How to get info from user
    /*
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getUserId();
        String username = userDetails.getUsername();

        log.info("Getting recipes for user: {} (ID: {})", username, userId);
        return recipeRepository.findByAuthorId(userId)
                .stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }
    */
}