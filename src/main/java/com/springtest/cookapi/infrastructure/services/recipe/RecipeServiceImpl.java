
package com.springtest.cookapi.infrastructure.services.recipe;

import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.exceptions.ForbiddenException;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.mappers.ProductMapper;
import com.springtest.cookapi.domain.mappers.RecipeMapper;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.CurrentUserService;
import com.springtest.cookapi.infrastructure.services.cloudinary.ICloudinaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ICloudinaryService cloudinaryService;

    @Override
    @Transactional
    @CacheEvict(value = "all-recipes", allEntries = true)
    public RecipeDto addRecipe(CreateRecipeDto createRecipeDto, MultipartFile image) throws IOException {
        Recipe recipe = recipeMapper.toRecipe(createRecipeDto);

        var currentUserId = currentUserService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));

        recipe.setUser(currentUser);

        List<Product> productsFromRecipe = recipe.getProductList();
        var products = addNotExistingProducts(productsFromRecipe);
        recipe.setProductList(products);

        if (image != null) {
            var uploadResult = cloudinaryService.addImageToCloudinary(image);
            String publicId = uploadResult.get("public_id").toString();
            String imageUrl = uploadResult.get("secure_url").toString();

            recipe.setPublicId(publicId);
            recipe.setImageUrl(imageUrl);
        }

        var savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toRecipeDto(savedRecipe);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "all-recipes", allEntries = true),
            @CacheEvict(value = "recipe", key = "'recipe_' + #recipeId.toString()")
    })
    public void deleteRecipe(Long recipeId) throws IOException {
        var recipeToDelete = getRecipeById(recipeId);
        String publicId = recipeToDelete.getPublicId();

        var currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserId.equals(recipeToDelete.getUser().getId())) {
            throw new ForbiddenException("You are not allowed to delete this recipe");
        }

        recipeRepository.deleteById(recipeId);
        if (publicId != null) {
            cloudinaryService.deleteImageFromCloudinary(publicId);
        }
    }



    @Override
    @Cacheable(value = "all-recipes", key = "#getRecipesRequest.toString()")
    public PageResponse<RecipeDto> getAllRecipes(GetRecipesRequest  getRecipesRequest) {

        Sort.Direction sortDirection = getRecipesRequest.sortDirection() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = getSortBy(getRecipesRequest.sortBy());

        PageRequest pageRequest = PageRequest.of(
                getRecipesRequest.limit(),
                getRecipesRequest.pageNumber(),
                Sort.by(sortDirection, sortBy)
        );

        var recipes = recipeRepository.findAll(pageRequest);
        return PageResponse.of(recipes.map(recipeMapper::toRecipeDto));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "all-recipes", allEntries = true),
            @CacheEvict(value = "recipe", key = "'recipe_' + #recipeId.toString()")
    })
    public RecipeDto updateRecipe(Long recipeId, UpdateRecipeDto updateRecipeDto, MultipartFile image) throws IOException {
        var recipeToModify = getRecipeById(recipeId);

        var currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserId.equals(recipeToModify.getUser().getId())) {
            throw new ForbiddenException("You are not allowed to modify this recipe");
        }

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

        if (image != null) {
            if (recipeToModify.getPublicId() != null) {
                cloudinaryService.deleteImageFromCloudinary(recipeToModify.getPublicId());
            }
            var uploadResult = cloudinaryService.addImageToCloudinary(image);
            String publicId = uploadResult.get("public_id").toString();
            String imageUrl = uploadResult.get("secure_url").toString();

            recipeToModify.setPublicId(publicId);
            recipeToModify.setImageUrl(imageUrl);
        }

        var savedRecipe = recipeRepository.save(recipeToModify);
        return recipeMapper.toRecipeDto(savedRecipe);
    }

    @Override
    @Cacheable(value = "recipe", key = "'recipe_' + #recipeId.toString()")
    public RecipeDto getRecipeDtoById(Long recipeId) {
        var recipe = getRecipeById(recipeId);
        return recipeMapper.toRecipeDto(recipe);
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
