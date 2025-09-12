package com.springtest.cookapi.domain.dtos.recipe;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRecipeDto (
        @NotEmpty
        String name,
        @NotEmpty
        String description,
        @NotNull
        Difficulty difficulty,
        @NotNull
        Double calories,
        @NotEmpty
        @Valid
        List<CreateProductDto> products
){}
