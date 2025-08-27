package com.springtest.cookapi.domain.exceptions;

public record ExceptionResponse (
        Integer code,
        String message
) {}
