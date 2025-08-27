package com.springtest.cookapi.infrastructure.services;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;

import java.util.List;

public interface IRecipeService {
    void addRecipe(CreateRecipeDto createRecipeDto);
    void deleteRecipe(Long recipeId);
    List<RecipeDto> getAllRecipes(GetRecipesRequest getRecipesRequest);
    void updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto);
}
