package com.springtest.cookapi.domain.dtos.product;

import jakarta.validation.constraints.NotEmpty;


public record CreateProductDto (
        @NotEmpty
        String name
){}
