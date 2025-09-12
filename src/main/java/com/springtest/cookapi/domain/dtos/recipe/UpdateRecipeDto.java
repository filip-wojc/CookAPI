package com.springtest.cookapi.domain.dtos.recipe;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.enums.Difficulty;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateRecipeDto (
        String name,
        String description,
        Difficulty difficulty,
        Double calories,
        @Valid
        List<CreateProductDto> products
){}
