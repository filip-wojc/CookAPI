package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import com.springtest.cookapi.infrastructure.services.recipe.IRecipeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> addRecipe(@Valid @RequestPart CreateRecipeDto createRecipeDto, @RequestPart(required = false) MultipartFile image) throws IOException {
        var addedRecipe = recipeService.addRecipe(createRecipeDto, image);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedRecipe.id())
                .toUri();

        return ResponseEntity.created(location).body(addedRecipe);
    }

    @PutMapping(value = "/{id}", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable Long id, @Valid @RequestPart UpdateRecipeDto updateRecipeDto, @RequestPart(required = false) MultipartFile image) throws IOException {

        var updatedRecipe = recipeService.updateRecipe(id, updateRecipeDto, image);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) throws IOException {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }


}
