package com.springtest.cookapi.domain.dtos.review;

import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import java.io.Serializable;

public record ReviewDto (
        Long id,
        String title,
        String reviewContent,
        Integer rating,
        RecipeDto recipe
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
