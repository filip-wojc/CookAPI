package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.exceptions.ForbiddenException;
import com.springtest.cookapi.domain.mappers.ProductMapper;
import com.springtest.cookapi.domain.mappers.RecipeMapper;
import com.springtest.cookapi.domain.requests.GetRecipesRequest;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.CurrentUserService;
import com.springtest.cookapi.infrastructure.services.recipe.RecipeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceUnitTests {
    @Mock
    RecipeRepository recipeRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    RecipeMapper recipeMapper;
    @Mock
    ProductMapper productMapper;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    RecipeServiceImpl recipeService;

    private CreateRecipeDto createRecipeDto;
    private User mockedUser;
    private Product existingProduct;
    private List<Product> allProductsInDb;
    private Recipe mappedRecipe;

    @BeforeAll
    public static void setUp() {

    }

    @BeforeEach
    public void beforeEach() {
        createRecipeDto = new CreateRecipeDto(
                "test",
                "test",
                Difficulty.MEDIUM,
                200d,
                new ArrayList<>(List.of(new CreateProductDto("test")))
        );

        mockedUser = new User();
        mockedUser.setId(1l);
        mockedUser.setUsername("test");

        existingProduct = new Product(1L, "test", null);

        allProductsInDb = new ArrayList<>(List.of(existingProduct));

        mappedRecipe = new Recipe(
                null,
                "test",
                "test",
                Difficulty.MEDIUM,
                200d,
                new ArrayList<>(List.of(new Product(null, "test", null))),
                null,
                null
        );
    }

    @Test
    public void getAllRecipesShouldReturnCorrectRecipes() {
        var getRecipeRequest = new GetRecipesRequest(
                SortBy.NAME,
                SortDirection.ASC,
                10
        );

        Recipe recipe1 = new Recipe(1L, "Recipe A", "desc", Difficulty.EASY, 100d, new ArrayList<>(), new ArrayList<>(), new User());
        Recipe recipe2 = new Recipe(2L, "Recipe C", "desc", Difficulty.MEDIUM, 200d, new ArrayList<>(), new ArrayList<>(), new User());
        Recipe recipe3 = new Recipe(3L, "Recipe B", "desc", Difficulty.HARD, 150d, new ArrayList<>(), new ArrayList<>(), new User());

        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2, recipe3));

        // Mock DTOs
        RecipeDto dto1 = new RecipeDto(1L, "Recipe A", "desc", "EASY", 100d, new ArrayList<>());
        RecipeDto dto2 = new RecipeDto(2L, "Recipe C", "desc", "MEDIUM", 200d, new ArrayList<>());
        RecipeDto dto3 = new RecipeDto(3L, "Recipe B", "desc", "HARD", 150d, new ArrayList<>());

        Mockito.when(recipeRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(recipePage);
        Mockito.when(recipeMapper.toRecipeDto(recipe1)).thenReturn(dto1);
        Mockito.when(recipeMapper.toRecipeDto(recipe2)).thenReturn(dto2);
        Mockito.when(recipeMapper.toRecipeDto(recipe3)).thenReturn(dto3);

        List<RecipeDto> result = recipeService.getAllRecipes(getRecipeRequest);

        Assertions.assertEquals(3, result.size());

    }

    @Test
    public void addRecipeShouldAddRecipeSuccessfully() {
        Mockito.when(currentUserService.getCurrentUserId()).thenReturn(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockedUser));
        Mockito.when(recipeMapper.toRecipe(createRecipeDto)).thenReturn(mappedRecipe);

        Recipe expectedSavedRecipe = new Recipe(
                1L,
                "test",
                "test",
                Difficulty.MEDIUM,
                200d,
                new ArrayList<>(List.of(existingProduct)),
                null,
                mockedUser
        );

        Mockito.when(recipeRepository.save(Mockito.any())).thenReturn(expectedSavedRecipe);
        Mockito.when(productRepository.findAll()).thenReturn(allProductsInDb);
        Mockito.when(productRepository.saveAll(Mockito.anyList())).thenReturn(new ArrayList<>(List.of(existingProduct)));


        Recipe addedRecipe = recipeService.addRecipe(createRecipeDto);

        assertRecipeBasicProperties(addedRecipe);
    }

    @Test
    public void deleteRecipeShouldDeleteRecipeSuccessfully() {
        Mockito.when(currentUserService.getCurrentUserId()).thenReturn(1L);

        Recipe recipeToDelete = new Recipe(1l, "test", "test", Difficulty.MEDIUM, 200d, new ArrayList<>(), new ArrayList<>(), mockedUser);
        Mockito.when(recipeRepository.findById(1l)).thenReturn(Optional.of(recipeToDelete));
        Mockito.doNothing().when(recipeRepository).deleteById(recipeToDelete.getId());

        recipeService.deleteRecipe(1l);

        Mockito.verify(recipeRepository, Mockito.times(1)).deleteById(recipeToDelete.getId());
    }

    @Test
    public void deleteRecipeShouldThrowForbiddenException() {
        Mockito.when(currentUserService.getCurrentUserId()).thenReturn(2l);

        Recipe recipeToDelete = new Recipe(1l, "test", "test", Difficulty.MEDIUM, 200d, new ArrayList<>(), new ArrayList<>(), mockedUser);
        Mockito.when(recipeRepository.findById(1l)).thenReturn(Optional.of(recipeToDelete));

        Assertions.assertThrows(ForbiddenException.class, () -> recipeService.deleteRecipe(1l));

        Mockito.verify(recipeRepository, Mockito.never()).deleteById(recipeToDelete.getId());
    }

    @Test
    public void testPrivateMethod_getSortBy() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var sortByMethod = RecipeServiceImpl.class.getDeclaredMethod("getSortBy", SortBy.class);
        sortByMethod.setAccessible(true);

        String result_name = sortByMethod.invoke(recipeService, SortBy.NAME).toString();
        String result_calories = sortByMethod.invoke(recipeService, SortBy.CALORIES).toString();
        String result_difficulty = sortByMethod.invoke(recipeService, SortBy.DIFFICULTY).toString();
        Assertions.assertEquals(result_name, "name");
        Assertions.assertEquals(result_calories, "calories");
        Assertions.assertEquals(result_difficulty, "difficulty");
    }

    @Test
    public void testPrivateMethod_addNotExistingProducts() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var productsInDb = List.of(
                new Product(1L, "test1", null),
                new Product(2L, "test2", null),
                new Product(3L, "test3", null)
        );

        var newProduct = new Product(4L, "new product", null);

        Mockito.when(productRepository.findAll()).thenReturn(productsInDb);
        Mockito.when(productRepository.saveAll(Mockito.any())).thenReturn(List.of(newProduct));

        var testedMethod = RecipeServiceImpl.class.getDeclaredMethod("addNotExistingProducts", List.class);
        testedMethod.setAccessible(true);

        var allProducts = new ArrayList<>(productsInDb);
        allProducts.add(newProduct);

        List<Product> result = (List<Product>) testedMethod.invoke(recipeService, new ArrayList<>(allProducts));

        Assertions.assertEquals(4, result.size());

        ArgumentCaptor<List<Product>> productCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(productRepository).saveAll(productCaptor.capture());
        List<Product> expectedSavedProducts =  productCaptor.getValue();

        Assertions.assertEquals(1, expectedSavedProducts.size());
        Assertions.assertEquals("new product", expectedSavedProducts.get(0).getName());
    }

    private void assertRecipeBasicProperties(Recipe recipe) {
        Assertions.assertEquals("test", recipe.getName());
        Assertions.assertEquals("test", recipe.getDescription());
        Assertions.assertEquals(Difficulty.MEDIUM, recipe.getDifficulty());
        Assertions.assertEquals(200d, recipe.getCalories());
        Assertions.assertEquals(mockedUser, recipe.getUser());
        Assertions.assertEquals(1, recipe.getProductList().size());
        Assertions.assertEquals("test", recipe.getProductList().get(0).getName());
    }
}
