package com.springtest.cookapi.infrastructure.services.recipe;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IRecipeService {
    RecipeDto addRecipe(CreateRecipeDto createRecipeDto);
    void deleteRecipe(Long recipeId);
    PageResponse<RecipeDto> getAllRecipes(GetRecipesRequest getRecipesRequest);
    RecipeDto getRecipeDtoById(Long recipeId);
    RecipeDto updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto);
}
