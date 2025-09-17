package com.springtest.cookapi.infrastructure.security;

import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.exceptions.BadRequestException;
import com.springtest.cookapi.domain.requests.LoginRequest;
import com.springtest.cookapi.domain.requests.RefreshTokenRequest;
import com.springtest.cookapi.domain.requests.RegisterRequest;
import com.springtest.cookapi.domain.responses.LoginResponse;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = new User().builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();


            LoginResponse loginResponse = jwtService.generateTokenPair(authentication, userId);
            return loginResponse;
        } catch (Exception e) {
            throw new BadRequestException("Invalid username or password");
        }

    }

    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {

        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String user = jwtService.extractUsernameFromToken(refreshToken);
        Long userId = jwtService.extractUserIdFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user);

        if (userDetails == null) {
            throw new IllegalArgumentException("User not found");
        }

        UsernamePasswordAuthenticationToken auth =  new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String accessToken = jwtService.generateAccessToken(auth, userId);
        Date expirationDate = jwtService.extractExpirationDateFromToken(accessToken);
        Date refreshExpirationDate = jwtService.extractExpirationDateFromToken(refreshToken);
        return new LoginResponse(userId, accessToken, refreshToken, expirationDate, refreshExpirationDate);
    }
}