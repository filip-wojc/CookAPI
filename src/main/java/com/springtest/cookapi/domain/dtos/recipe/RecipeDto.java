package com.springtest.cookapi.domain.dtos.recipe;

import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.entities.Review;

import java.io.Serializable;
import java.util.List;

public record RecipeDto (
    Long id,
    String name,
    String description,
    String difficulty,
    Double calories,
    List<ProductDto> products
) implements Serializable{
    private static final long serialVersionUID = 1L;
}
