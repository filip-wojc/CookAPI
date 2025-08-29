package com.springtest.cookapi.domain.responses;

import lombok.AllArgsConstructor;
import lombok.Data;


public record TokenPair (
        String accessToken,
        String refreshToken
){}



