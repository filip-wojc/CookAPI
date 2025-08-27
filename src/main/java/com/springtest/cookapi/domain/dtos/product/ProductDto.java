package com.springtest.cookapi.domain.dtos.product;

import java.io.Serializable;

public record ProductDto (
        Long id,
        String name
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

