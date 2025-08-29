package com.springtest.cookapi.domain.mappers;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.entities.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

    private final ProductMapper productMapper;
    public Recipe toRecipe(CreateRecipeDto dto) {
        return new Recipe(
                null,
                dto.name(),
                dto.description(),
                dto.difficulty(),
                dto.calories(),
                dto.products().stream().map(productMapper::toEntity).toList(),
                new ArrayList<>(),
                null
        );
    }

    public RecipeDto toRecipeDto(Recipe recipe) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getDifficulty().toString(),
                recipe.getCalories(),
                recipe.getProductList().stream().map(productMapper::toDto).toList()
        );
    }
}
