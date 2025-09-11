package com.springtest.cookapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
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
import com.springtest.cookapi.infrastructure.services.cloudinary.CloudinaryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.cloudinary.url=cloudinary://fake_key:$fake_secret@fake_cloud"
})
@Import({TestSecurityConfig.class, TestCloudinaryConfig.class})
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
    @Autowired
    CloudinaryService cloudinaryService;

    static UserRepository staticUserRepository;
    static RecipeRepository staticRecipeRepository;
    static ProductRepository staticProductRepository;
    static ReviewRepository staticReviewRepository;
    static User staticTestUser1;
    static User staticTestUser2;
    static List<Long> savedRecipeIds;
    static List<Long> savedReviewIds;

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

        savedReviewIds = new ArrayList<>();
        savedRecipeIds = new ArrayList<>();

        seedTestData();
    }

    @Test
    void getReviewsShouldReturnOkAndContainsCorrectData() throws Exception {
        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("test_review3")))
                .andExpect(jsonPath("$.content[0].reviewContent", is("review3_desc")))
                .andExpect(jsonPath("$.content[0].rating", is(3)))
                .andExpect(jsonPath("$.content[0].recipeId", is(savedRecipeIds.get(0).intValue())))
                .andExpect(jsonPath("$.content[0].author.username", is("test_user_2")));
    }

    @Test
    void getReviewsShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0) + 100l)
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        ).andExpect(status().isNotFound());
    }

    @Test
    void getReviewsShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0) + 100l)
                .param("sortDirection", "ASC")
                .param("limit", "5")
                // pageNumber less than 0
                .param("pageNumber", "-1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void gerReviewByIdShouldReturnOkAndContainsCorrectData() throws Exception {
        mockMvc.perform(get("/api/review/{reviewId}", savedReviewIds.get(3))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("test_review4")))
                .andExpect(jsonPath("$.reviewContent", is("review4_desc")))
                .andExpect(jsonPath("$.rating", is(2)))
                .andExpect(jsonPath("$.recipeId", is(savedRecipeIds.get(1).intValue())))
                .andExpect(jsonPath("$.author.username", is("test_user_2")));
    }

    @Test
    void gerReviewByIdShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/review/{reviewId}", savedReviewIds.get(0) + 100l)
                ).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("test_user_2")
    void getAllReviewsShouldCacheResults() throws Exception {
        String cacheKey = "all-reviews::reviews" + savedRecipeIds.get(2).toString() + " ASC 5 0";
        stringRedisTemplate.delete(cacheKey);

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(2))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        );

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isTrue();

        var createReviewDto = new CreateReviewDto("new_review", "test", 5);

        mockMvc.perform(post("/api/review/recipe/{recipeId}", savedRecipeIds.get(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReviewDto))
        ).andExpect(status().isCreated());

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    void getAllRecipesShouldUseRedisCache() throws Exception {
        String cacheKey = "all-reviews::reviews" + savedRecipeIds.get(0).toString() + " ASC 5 0";
        stringRedisTemplate.delete(cacheKey);

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        );
        String firstValue = stringRedisTemplate.opsForValue().get(cacheKey);

        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        );
        String secondValue = stringRedisTemplate.opsForValue().get(cacheKey);


        assertThat(firstValue).isEqualTo(secondValue);
    }

    @Test
    void shouldBeFasterFromCache() throws Exception {
        String cacheKey = "all-reviews::reviews" + savedRecipeIds.get(0).toString() + " ASC 5 0";
        stringRedisTemplate.delete(cacheKey);
        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        long start =  System.currentTimeMillis();
        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        );
        long databaseQueryTime = System.currentTimeMillis() - start;

        System.out.println("Database query time: " + databaseQueryTime);

        start = System.currentTimeMillis();
        mockMvc.perform(get("/api/review/recipe/{recipeId}", savedRecipeIds.get(0))
                .param("sortDirection", "ASC")
                .param("limit", "5")
                .param("pageNumber", "0")
        );
        long cacheQueryTime = System.currentTimeMillis() - start;

        System.out.println("Cache query time: " + cacheQueryTime);

        assertThat(cacheQueryTime).isLessThan(databaseQueryTime);
    }

    @Test
    @WithUserDetails("test_user_2")
    void addReviewShouldReturnCreatedAndReturnCorrectReviewData() throws Exception {
        var newReviewDto = new CreateReviewDto("new_review", "test", 9);

        mockMvc.perform(post("/api/review/recipe/{recipeId}", savedRecipeIds.get(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReviewDto))
        ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("new_review")))
                .andExpect(jsonPath("$.reviewContent", is("test")))
                .andExpect(jsonPath("$.rating", is(9)))
                .andExpect(jsonPath("$.recipeId", is(savedRecipeIds.get(2).intValue())))
                .andExpect(jsonPath("$.author.username", is("test_user_2")));
    }

    @Test
    @WithUserDetails("test_user_2")
    void addReviewShouldReturnBadRequest() throws Exception {
        // max rating exceeded
        var newReviewDto = new CreateReviewDto("new_review", "test", 11);

        mockMvc.perform(post("/api/review/recipe/{recipeId}", savedRecipeIds.get(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReviewDto))
                ).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("test_user")
    void addReviewShouldReturnForbidden() throws Exception {
        var newReviewDto = new CreateReviewDto("new_review", "test", 5);

        mockMvc.perform(post("/api/review/recipe/{recipeId}", savedRecipeIds.get(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReviewDto))
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("test_user")
    void deleteReviewShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/review/{reviewId}", savedRecipeIds.get(0)))
                .andExpect(status().isNoContent());

        var deletedReview = staticReviewRepository.findById(savedRecipeIds.get(0));
        assertThat(deletedReview.isPresent()).isFalse();
    }

    @Test
    @WithUserDetails("test_user")
    void deleteReviewShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/review/{reviewId}", savedRecipeIds.get(3)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("test_user")
    void deleteReviewShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/review/{reviewId}", savedRecipeIds.get(0) + 100l))
                .andExpect(status().isNotFound());
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
                new Recipe(null, "recipe1", "recipe1_desc", Difficulty.EASY, 200d,null, null, List.of(product1, product2), null, user1),
                new Recipe(null, "recipe2", "recipe2_desc", Difficulty.MEDIUM, 300d,null, null, List.of(product1, product2, product3), null, user1),
                new Recipe(null, "recipe3", "recipe3_desc", Difficulty.MEDIUM, 250d,null, null, List.of(product2), null, user1),
                new Recipe(null, "recipe4", "recipe4_desc", Difficulty.HARD, 600d,null, null, List.of(product4, product2, product1), null, user2),
                new Recipe(null, "recipe5", "recipe5_desc", Difficulty.EASY, 150d,null, null, List.of(product3, product4), null, user2)
        );

        recipes = staticRecipeRepository.saveAllAndFlush(recipes);
        recipes.stream().forEach(recipe -> {savedRecipeIds.add(recipe.getId());});

        var reviews = List.of(
                new Review(null, "test_review1", "review1_desc", 6, recipes.get(3), user1),
                new Review(null, "test_review2", "review2_desc", 10, recipes.get(4), user1),
                new Review(null, "test_review3", "review3_desc", 3, recipes.get(0), user2),
                new Review(null, "test_review4", "review4_desc", 2, recipes.get(1), user2)
        );

        reviews = staticReviewRepository.saveAllAndFlush(reviews);
        reviews.stream().forEach(review -> {savedReviewIds.add(review.getId());});
    }
}
