package com.springtest.cookapi.domain.responses;

public record ExceptionResponse (
        Integer code,
        String message
) {}
