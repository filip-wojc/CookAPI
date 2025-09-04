package com.springtest.cookapi.domain.dtos.review;

import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.user.UserDto;

import java.io.Serializable;

public record ReviewDto (
        Long id,
        String title,
        String reviewContent,
        Integer rating,
        UserDto author,
        Long recipeId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
