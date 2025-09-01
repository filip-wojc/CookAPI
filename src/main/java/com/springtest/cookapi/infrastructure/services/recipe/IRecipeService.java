package com.springtest.cookapi.infrastructure.services.recipe;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;

import java.util.List;

public interface IRecipeService {
    Recipe addRecipe(CreateRecipeDto createRecipeDto);
    void deleteRecipe(Long recipeId);
    List<RecipeDto> getAllRecipes(GetRecipesRequest getRecipesRequest);
    RecipeDto getRecipeDtoById(Long recipeId);
    void updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto);
}
