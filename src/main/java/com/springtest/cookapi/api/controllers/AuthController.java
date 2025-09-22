package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.requests.LoginRequest;
import com.springtest.cookapi.domain.requests.RefreshTokenRequest;
import com.springtest.cookapi.domain.requests.RegisterRequest;
import com.springtest.cookapi.domain.responses.LoginResponse;
import com.springtest.cookapi.infrastructure.security.AuthService;
import com.springtest.cookapi.infrastructure.services.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping()
    public ResponseEntity<?> getUser() {
        var user = currentUserService.getCurrentUserDto();
        return ResponseEntity.ok(user);
    }

}