package com.springtest.cookapi.domain.responses;

import java.util.Date;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        Date tokenExpiration,
        Date refreshTokenExpiration
){}



