package com.springtest.cookapi.domain.dtos.user;

import java.io.Serializable;

public record UserDto (
        Long id,
        String fullname,
        String username
) implements Serializable {}
