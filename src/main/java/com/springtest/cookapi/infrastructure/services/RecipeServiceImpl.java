
package com.springtest.cookapi.infrastructure.services;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.mappers.ProductMapper;
import com.springtest.cookapi.domain.mappers.RecipeMapper;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements IRecipeService{
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final RecipeMapper recipeMapper;
    private final ProductMapper productMapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "all-recipes", key = "'recipes_list'")
    public void addRecipe(CreateRecipeDto createRecipeDto) {
        Recipe recipe = recipeMapper.toRecipe(createRecipeDto);

        var currentUserId = currentUserService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));;

        recipe.setUser(currentUser);

        List<Product> productsFromRecipe = recipe.getProductList();
        var products = addNotExistingProducts(productsFromRecipe);
        recipe.setProductList(products);
        recipeRepository.save(recipe);
    }

    @Transactional
    @CacheEvict(value = "all-recipes", key = "'recipes_list'")
    public void deleteRecipe(Long recipeId) {
        getRecipeById(recipeId);
        recipeRepository.deleteById(recipeId);
    }



    @Cacheable(value = "all-recipes", key = "#getRecipesRequest.toString()")
    public List<RecipeDto> getAllRecipes(GetRecipesRequest  getRecipesRequest) {

        Sort.Direction sortDirection = getRecipesRequest.sortDirection() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = getSortBy(getRecipesRequest.sortBy());

        PageRequest pageRequest = PageRequest.of(
                0,
                getRecipesRequest.limit(),
                Sort.by(sortDirection, sortBy)
        );

        var recipes = recipeRepository.findAll(pageRequest);
        var recipeDtos = recipes.stream().map(recipeMapper::toRecipeDto).collect(Collectors.toList());
        return recipeDtos;

    }

    @Transactional
    @CacheEvict(value = "all-recipes", key = "'recipes_list'")
    public void updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto) {
        var recipeToModify = getRecipeById(recipeId);

        if (updateRecipeDto.name() != null) {
            recipeToModify.setName(updateRecipeDto.name());
        }
        if (updateRecipeDto.description() != null) {
            recipeToModify.setDescription(updateRecipeDto.description());
        }
        if (updateRecipeDto.difficulty() != null) {
            recipeToModify.setDifficulty(updateRecipeDto.difficulty());
        }
        if (updateRecipeDto.calories() != null) {
            recipeToModify.setCalories(updateRecipeDto.calories());
        }
        if (!updateRecipeDto.products().isEmpty()) {
            var products = updateRecipeDto.products().stream().map(productMapper::toEntity).collect(Collectors.toList());
            var managedProducts = addNotExistingProducts(products);
            recipeToModify.setProductList(managedProducts);
        }

        recipeRepository.save(recipeToModify);

    }


    private Recipe getRecipeById(Long recipeId) throws NotFoundException {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (!recipe.isPresent()) {
            throw new NotFoundException("Recipe with id " + recipeId + " not found");
        }
        return recipe.get();
    }

    private String getSortBy(SortBy sortBy) {
        return switch (sortBy) {
            case NAME -> "name";
            case CALORIES -> "calories";
            case DIFFICULTY -> "difficulty";
        };
    }

    private List<Product> addNotExistingProducts(List<Product> newProducts) {
        List<Product> allProducts = productRepository.findAll();

        List<Product> productsToSave = new ArrayList<>();
        List<Product> existingProducts = new ArrayList<>();

        Set<String> allProductNames = allProducts.stream().map(Product::getName).collect(Collectors.toSet());

        for (Product product : newProducts) {
            if (allProductNames.contains(product.getName())) {
                var existingProduct = allProducts.stream().filter(p -> p.getName().equals(product.getName())).findFirst().orElse(null);
                if (existingProduct != null) {
                    existingProducts.add(existingProduct);
                }
            }
            else {
                productsToSave.add(product);
            }
        }

        var savedProducts = productRepository.saveAll(productsToSave);

        List<Product> allProductsFromRecipe = new ArrayList<>();
        allProductsFromRecipe.addAll(savedProducts);
        allProductsFromRecipe.addAll(existingProducts);

        return allProductsFromRecipe;
    }
}
