package com.springtest.cookapi.infrastructure.services.recipe;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IRecipeService {
    RecipeDto addRecipe(CreateRecipeDto createRecipeDto, MultipartFile image) throws IOException;
    void deleteRecipe(Long recipeId) throws IOException;
    PageResponse<RecipeDto> getAllRecipes(GetRecipesRequest getRecipesRequest);
    RecipeDto getRecipeDtoById(Long recipeId);
    RecipeDto updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto, MultipartFile image) throws IOException;
}
