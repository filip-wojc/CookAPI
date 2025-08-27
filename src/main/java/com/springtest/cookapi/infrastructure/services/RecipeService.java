
package com.springtest.cookapi.infrastructure.services;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.mappers.RecipeMapper;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final RecipeMapper recipeMapper;

    @Transactional
    public void addRecipe(CreateRecipeDto createRecipeDto) {
        Recipe recipe = recipeMapper.toRecipe(createRecipeDto);

        List<Product> allProducts = productRepository.findAll();
        List<Product> productsFromRecipe = recipe.getProductList();

        List<Product> productsToSave = new ArrayList<>();
        List<Product> existingProducts = new ArrayList<>();

        Set<String> allProductNames = allProducts.stream().map(Product::getName).collect(Collectors.toSet());

        for (Product product : productsFromRecipe) {
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

        recipe.setProductList(allProductsFromRecipe);

        recipeRepository.save(recipe);
    }


    @Cacheable(value = "all-recipes", key = "'recipes_list'")
    public List<RecipeDto> getAllRecipes() {
        var recipies = recipeRepository.findAll();
        var recipeDtos = recipies.stream().map(recipeMapper::toRecipeDto).collect(Collectors.toList());
        return recipeDtos;
    }
}
