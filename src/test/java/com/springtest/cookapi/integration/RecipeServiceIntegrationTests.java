package com.springtest.cookapi.integration;


import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.Review;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.recipe.IRecipeService;
import com.springtest.cookapi.infrastructure.services.recipe.RecipeServiceImpl;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Testcontainers
@Import(TestSecurityConfig.class)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RecipeServiceIntegrationTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    static UserRepository staticUserRepository;
    static User staticTestUser;

    @Autowired
    private IRecipeService recipeService;

    @Autowired
    private RecipeRepository recipeRepository;
    private CreateRecipeDto createRecipeDto;
    private CreateRecipeDto wrongCreateRecipeDto;

    @Test
    void connectionEstablished() {
        Assertions.assertThat(postgreSQLContainer.isCreated()).isTrue();
        Assertions.assertThat(postgreSQLContainer.isRunning()).isTrue();

        Assertions.assertThat(redisContainer.isCreated()).isTrue();
        Assertions.assertThat(redisContainer.isRunning()).isTrue();
    }

    @BeforeAll
    static void init(@Autowired UserRepository userRepository) {
        staticUserRepository = userRepository;

        staticTestUser = new User("test","test_user", "test_password", Role.USER);
        staticTestUser = staticUserRepository.save(staticTestUser);
    }

    @BeforeEach
    void setup() {


        var products1 = List.of(
                new Product(1l, "test_pro1", null),
                new Product(2l, "test_pro2", null),
                new Product(3l, "test_pro3", null)
        );
        var products2 = List.of(
                new Product(4l, "test_pro4", null),
                new Product(5l, "test_pro5", null),
                new Product(6l, "test_pro6", null)
        );
        var createProducts1Dto = List.of(
                new CreateProductDto("test_pro1"),
                new CreateProductDto("test_pro2"),
                new CreateProductDto("test_pro3")
        );
        var createProducts2Dto = List.of(
                new CreateProductDto("test_pro4"),
                new CreateProductDto("test_pro5"),
                new CreateProductDto("test_pro6")
        );
        var recipes = List.of(
                new Recipe(1l, "test1", "test_d1", Difficulty.MEDIUM, 200d, products1, null, staticTestUser),
                new Recipe(2l, "test2", "test_d2", Difficulty.EASY, 300d, products2, null, staticTestUser)
        );

        var createRecipesDto = List.of(
                new CreateRecipeDto("test1", "test_d1", Difficulty.MEDIUM, 200d, createProducts1Dto),
                new CreateRecipeDto("", "test_d2", Difficulty.EASY, 300d, createProducts2Dto)
        );
        this.createRecipeDto = createRecipesDto.get(0);
        this.wrongCreateRecipeDto = createRecipesDto.get(1);
    }

    @Test
    @WithUserDetails("test_user")
    void createRecipeShouldBeSuccessful() {
        var recipe = recipeService.addRecipe(this.createRecipeDto);
        Assertions.assertThat(recipe.id()).isNotNull();
        Assertions.assertThat(recipe.name()).isEqualTo("test1");
    }

}
