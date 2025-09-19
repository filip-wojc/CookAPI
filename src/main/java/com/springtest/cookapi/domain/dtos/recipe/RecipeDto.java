package com.springtest.cookapi.domain.dtos.recipe;

import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.dtos.user.UserDto;

import java.io.Serializable;
import java.util.List;

public record RecipeDto (
    Long id,
    String name,
    String description,
    String difficulty,
    Double calories,
    Double rating,
    String imageUrl,
    List<ProductDto> products,
    UserDto author
) implements Serializable{
    private static final long serialVersionUID = 1L;
}
