package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import com.springtest.cookapi.infrastructure.services.recipe.IRecipeService;
import com.springtest.cookapi.infrastructure.services.recipe.RecipeServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final IRecipeService recipeService;

    @GetMapping
    public ResponseEntity<PageResponse<RecipeDto>> getAllRecipes(@RequestParam SortBy sortBy, @RequestParam SortDirection sortDirection, @RequestParam @Min(1) @Max(50) Integer limit, @RequestParam @Min(0) Integer pageNumber) {
        return ResponseEntity.ok(recipeService.getAllRecipes(new GetRecipesRequest(sortBy, sortDirection, limit, pageNumber)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeDtoById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeDto> addRecipe(@Valid @RequestBody CreateRecipeDto recipeDto){
        var addedRecipe = recipeService.addRecipe(recipeDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedRecipe.id())
                .toUri();

        return ResponseEntity.created(location).body(addedRecipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable Long id, @Valid @RequestBody UpdateRecipeDto updateRecipeDto){
        var updatedRecipe = recipeService.updateRecipe(id, updateRecipeDto);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id){
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
