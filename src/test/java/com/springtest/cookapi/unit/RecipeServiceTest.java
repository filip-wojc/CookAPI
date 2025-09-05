package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.services.recipe.RecipeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {
    @Mock
    ProductRepository productRepository;
    @InjectMocks
    RecipeServiceImpl recipeService;


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

}
