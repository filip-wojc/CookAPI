package com.springtest.cookapi.domain.responses;

public record TokenPair (
        String accessToken,
        String refreshToken
){}



