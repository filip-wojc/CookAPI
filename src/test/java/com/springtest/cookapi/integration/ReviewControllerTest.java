package com.springtest.cookapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.Review;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.ReviewRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
public class ReviewControllerTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    static UserRepository staticUserRepository;
    static RecipeRepository staticRecipeRepository;
    static ProductRepository staticProductRepository;
    static ReviewRepository staticReviewRepository;
    static User staticTestUser1;
    static User staticTestUser2;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired RecipeRepository recipeRepository,
            @Autowired ProductRepository productRepository,
            @Autowired ReviewRepository reviewRepository
    ) {
        staticUserRepository = userRepository;

        staticTestUser1 = new User("test","test_user", "test_password", Role.USER);
        staticTestUser2 = new User("test_2", "test_user_2", "test_password", Role.USER);

        staticTestUser1 = staticUserRepository.save(staticTestUser1);
        staticTestUser2 = staticUserRepository.save(staticTestUser2);

        staticRecipeRepository = recipeRepository;
        staticProductRepository = productRepository;
        staticReviewRepository = reviewRepository;

        seedTestData();
    }

    private static void seedTestData() {
        User user1 = staticUserRepository.findById(staticTestUser1.getId()).orElseThrow();
        User user2 = staticUserRepository.findById(staticTestUser2.getId()).orElseThrow();

        var products = List.of(
                new Product(null,"product1", null),
                new Product(null, "product2", null),
                new Product(null, "product3", null),
                new Product(null, "product4", null)
        );

        products = staticProductRepository.saveAllAndFlush(products);

        Product product1 = products.get(0);
        Product product2 = products.get(1);
        Product product3 = products.get(2);
        Product product4 = products.get(3);

        var recipes = List.of(
                new Recipe(null, "recipe1", "recipe1_desc", Difficulty.EASY, 200d, List.of(product1, product2), null, user1),
                new Recipe(null, "recipe2", "recipe2_desc", Difficulty.MEDIUM, 300d, List.of(product1, product2, product3), null, user1),
                new Recipe(null, "recipe3", "recipe3_desc", Difficulty.MEDIUM, 250d, List.of(product2), null, user1),
                new Recipe(null, "recipe4", "recipe4_desc", Difficulty.HARD, 600d, List.of(product4, product2, product1), null, user2),
                new Recipe(null, "recipe5", "recipe5_desc", Difficulty.EASY, 150d, List.of(product3, product4), null, user2)
        );

        recipes = staticRecipeRepository.saveAllAndFlush(recipes);

        var reviews = List.of(
                new Review(null, "test_review1", "review1_desc", 6, recipes.get(3), user1),
                new Review(null, "test_review2", "review2_desc", 6, recipes.get(4), user1),
                new Review(null, "test_review3", "review3_desc", 6, recipes.get(0), user2),
                new Review(null, "test_review4", "review4_desc", 6, recipes.get(1), user2)
        );

        staticReviewRepository.saveAllAndFlush(reviews);
    }
}
