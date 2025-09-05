package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.RecipeDto;
import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.mappers.ProductMapper;
import com.springtest.cookapi.domain.mappers.RecipeMapper;
import com.springtest.cookapi.domain.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeMapperTest {
    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RecipeMapper recipeMapper;

    @Test
    void shouldMapCreateRecipeDtoToRecipe() {
        List<CreateProductDto> productDtos = List.of(
                new CreateProductDto("Product1"),
                new CreateProductDto("Product2")
        );

        CreateRecipeDto dto = new CreateRecipeDto(
                "Test Recipe",
                "Test Description",
                Difficulty.MEDIUM,
                250.0,
                productDtos
        );

        Product product1 = new Product(null, "Product1", new ArrayList<>());
        Product product2 = new Product(null, "Product2", new ArrayList<>());

        when(productMapper.toEntity(productDtos.get(0))).thenReturn(product1);
        when(productMapper.toEntity(productDtos.get(1))).thenReturn(product2);

        Recipe recipe = recipeMapper.toRecipe(dto);

        assertThat(recipe).isNotNull();
        assertThat(recipe.getId()).isNull();
        assertThat(recipe.getName()).isEqualTo("Test Recipe");
        assertThat(recipe.getDescription()).isEqualTo("Test Description");
        assertThat(recipe.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(recipe.getCalories()).isEqualTo(250.0);
        assertThat(recipe.getProductList()).hasSize(2);
        assertThat(recipe.getReviewList()).isNotNull().isEmpty();
        assertThat(recipe.getUser()).isNull();

        verify(productMapper).toEntity(productDtos.get(0));
        verify(productMapper).toEntity(productDtos.get(1));
    }

    @Test
    void shouldMapRecipeEntityToDto() {
        User user = new User("Test", "testuser", "password", Role.USER);
        user.setId(1L);

        List<Product> products = List.of(
                new Product(1L, "Product1", new ArrayList<>()),
                new Product(2L, "Product2", new ArrayList<>())
        );

        Recipe recipe = new Recipe(
                1L, "Test Recipe", "Description", Difficulty.HARD, 300.0,
                products, new ArrayList<>(), user
        );

        ProductDto productDto1 = new ProductDto(1L, "Product1");
        ProductDto productDto2 = new ProductDto(2L, "Product2");
        UserDto userDto = new UserDto(1L, "Test", "testuser");

        when(productMapper.toDto(products.get(0))).thenReturn(productDto1);
        when(productMapper.toDto(products.get(1))).thenReturn(productDto2);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        RecipeDto dto = recipeMapper.toRecipeDto(recipe);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Test Recipe");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.difficulty()).isEqualTo("HARD");
        assertThat(dto.calories()).isEqualTo(300.0);
        assertThat(dto.products()).hasSize(2);
        assertThat(dto.author()).isEqualTo(userDto);


        verify(productMapper).toDto(products.get(0));
        verify(productMapper).toDto(products.get(1));
        verify(userMapper).toUserDto(user);
    }
}
