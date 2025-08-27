package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.infrastructure.services.RecipeServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeServiceImpl recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDto>> getAllRecipes(@RequestParam SortBy sortBy, @RequestParam SortDirection sortDirection, @RequestParam @Min(1) @Max(50) Integer limit) {
        return ResponseEntity.ok(recipeService.getAllRecipes(new GetRecipesRequest(sortBy, sortDirection, limit)));
    }

    @PostMapping
    public ResponseEntity<?> addRecipe(@Valid @RequestBody CreateRecipeDto recipeDto){
        recipeService.addRecipe(recipeDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @Valid @RequestBody UpdateRecipeDto updateRecipeDto){
        recipeService.updateRecipe(id, updateRecipeDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id){
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
