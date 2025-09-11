package com.springtest.cookapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.dtos.recipe.UpdateRecipeDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.infrastructure.repositories.ProductRepository;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.cloudinary.CloudinaryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
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
public class RecipeControllerTest {
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
    static User staticLoggedTestUser;
    static User staticTestUser;

    static List<Long> savedRecipeIds;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired RecipeRepository recipeRepository,
            @Autowired ProductRepository productRepository
    ) {
        staticUserRepository = userRepository;

        staticLoggedTestUser = new User("test","test_user", "test_password", Role.USER);
        staticTestUser = new User("test2", "test_user_unauthorized", "test_password", Role.USER);

        staticLoggedTestUser = staticUserRepository.save(staticLoggedTestUser);
        staticTestUser = staticUserRepository.save(staticTestUser);

        staticRecipeRepository = recipeRepository;
        staticProductRepository = productRepository;

        savedRecipeIds = new ArrayList<>();

        seedTestData();
    }

    @Test
    void getAllRecipesShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/recipe")
                        .param("sortBy", SortBy.CALORIES.toString())
                        .param("sortDirection", SortDirection.ASC.toString())
                        .param("limit", "5")
                        .param("pageNumber", "0")
                )
                .andExpect(status().isOk());
    }

    @Test
    void getAllRecipesShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipe")
                        .param("sortBy", SortBy.CALORIES.toString())
                        .param("sortDirection", SortDirection.ASC.toString())
                        // exceeded max limit
                        .param("limit", "51")
                        .param("pageNumber", "0")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRecipesShouldBeSortedByCaloriesAsc() throws Exception {
        mockMvc.perform(get("/api/recipe")
                        .param("sortBy", SortBy.CALORIES.toString())
                        .param("sortDirection", SortDirection.ASC.toString())
                        .param("limit", "5")
                        .param("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].calories", is(150d)))
                .andExpect(jsonPath("$.content[0].products", hasSize(2)))
                .andExpect(jsonPath("$.content[0].products[0].name", is("product3")));
    }

    @Test
    void getAllRecipesShouldBeSortedByCaloriesDesc() throws Exception {
        mockMvc.perform(get("/api/recipe")
                        .param("sortBy", SortBy.CALORIES.toString())
                        .param("sortDirection", SortDirection.DESC.toString())
                        .param("limit", "5")
                        .param("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[4].calories", is(150d)))
                .andExpect(jsonPath("$.content[4].products", hasSize(2)))
                .andExpect(jsonPath("$.content[4].products[0].name", is("product3")));
    }


    @Test
    @WithUserDetails("test_user")
    void getAllRecipesShouldCacheResults() throws Exception {
        String cacheKey = "all-recipes::CALORIES ASC 5 0";
        stringRedisTemplate.delete(cacheKey);

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        mockMvc.perform(get("/api/recipe?sortBy=CALORIES&sortDirection=ASC&limit=5&pageNumber=0"));

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isTrue();

        var createProductsDto = List.of(
                new CreateProductDto("test_pro1"),
                new CreateProductDto("test_pro2"),
                new CreateProductDto("test_pro3")
        );
        var createRecipeDto = new CreateRecipeDto("test1", "test_d1", Difficulty.MEDIUM, 200d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "createRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(createRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );


        mockMvc.perform(multipart("/api/recipe")
                .file(recipeData)
                .file(imageFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isCreated());

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    void getAllRecipesShouldUseRedisCache() throws Exception {
        String cacheKey = "all-recipes::CALORIES ASC 5 0";
        stringRedisTemplate.delete(cacheKey);

        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        mockMvc.perform(get("/api/recipe?sortBy=CALORIES&sortDirection=ASC&limit=5&pageNumber=0"));
        String firstValue = stringRedisTemplate.opsForValue().get(cacheKey);

        mockMvc.perform(get("/api/recipe?sortBy=CALORIES&sortDirection=ASC&limit=5&pageNumber=0"));
        String secondValue = stringRedisTemplate.opsForValue().get(cacheKey);


        assertThat(firstValue).isEqualTo(secondValue);
    }

    @Test
    void shouldBeFasterFromCache() throws Exception {
        String cacheKey = "all-recipes::CALORIES ASC 5 0";
        stringRedisTemplate.delete(cacheKey);
        assertThat(stringRedisTemplate.hasKey(cacheKey)).isFalse();

        long start =  System.currentTimeMillis();
        mockMvc.perform(get("/api/recipe?sortBy=CALORIES&sortDirection=ASC&limit=5&pageNumber=0"));
        long databaseQueryTime = System.currentTimeMillis() - start;

        System.out.println("Database query time: " + databaseQueryTime);

        start = System.currentTimeMillis();
        mockMvc.perform(get("/api/recipe?sortBy=CALORIES&sortDirection=ASC&limit=5&pageNumber=0"));
        long cacheQueryTime = System.currentTimeMillis() - start;

        System.out.println("Cache query time: " + cacheQueryTime);

        assertThat(cacheQueryTime).isLessThan(databaseQueryTime);
    }

    @Test
    void getRecipeByIdShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/recipe/{id}", savedRecipeIds.get(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("recipe1")))
                .andExpect(jsonPath("$.description", is("recipe1_desc")))
                .andExpect(jsonPath("$.difficulty", is(Difficulty.EASY.toString())))
                .andExpect(jsonPath("$.calories", is(200d)))
                .andExpect(jsonPath("$.products", hasSize(2)));
    }

    @Test
    void getRecipeByIdShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/recipe/{id}", savedRecipeIds.get(0) + 100l))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("test_user")
    void addRecipeShouldReturnCreated() throws Exception {
        var createProductsDto = List.of(
                new CreateProductDto("test_pro1"),
                new CreateProductDto("test_pro2"),
                new CreateProductDto("test_pro3")
        );
        var createRecipeDto = new CreateRecipeDto("test1", "test_d1", Difficulty.MEDIUM, 200d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "createRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(createRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipe")
                        .file(recipeData)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("test1")))
                .andExpect(jsonPath("$.products[0].name", is("test_pro1")))
                .andExpect(jsonPath("$.author.username", is("test_user")))
                .andExpect(jsonPath("$.imageUrl", is("https://test-cloudinary.com/test-image.jpg")));
    }

    @Test
    @WithUserDetails("test_user")
    void addRecipeShouldThrowValidationError() throws Exception {
        var createProductsDto = List.of(
                new CreateProductDto("test_pro1"),
                new CreateProductDto("test_pro2"),
                new CreateProductDto("test_pro3")
        );
        var createRecipeDto = new CreateRecipeDto("", "test_d1", Difficulty.MEDIUM, 200d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "createRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(createRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipe")
                        .file(recipeData)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("test_user")
    void deleteRecipeShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/recipe/{id}", savedRecipeIds.get(0))
        ).andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("test_user")
    void deleteRecipeShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/recipe/{id}", savedRecipeIds.get(4))
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("test_user")
    void deleteRecipeShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/recipe/{id}", savedRecipeIds.get(0) + 100l)
        ).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("test_user")
    void updateRecipeShouldReturnOk() throws Exception {
        var createProductsDto = List.of(
                new CreateProductDto("updated_pro1"),
                new CreateProductDto("updated_pro2"),
                new CreateProductDto("updated_pro3")
        );
        var updateRecipeDto = new UpdateRecipeDto("recipe1_updated", "recipe1_desc_updated", Difficulty.HARD, 2000d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "updateRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(updateRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipe/{id}", savedRecipeIds.get(0))
                        .file(recipeData)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("recipe1_updated")))
                .andExpect(jsonPath("$.description", is("recipe1_desc_updated")))
                .andExpect(jsonPath("$.difficulty", is(Difficulty.HARD.toString())))
                .andExpect(jsonPath("$.calories", is(2000d)))
                .andExpect(jsonPath("$.products", hasSize(3)))
                .andExpect(jsonPath("$.imageUrl", is("https://test-cloudinary.com/test-image.jpg")));
    }

    @Test
    @WithUserDetails("test_user")
    void updateRecipeShouldReturnForbidden() throws Exception {
        var createProductsDto = List.of(
                new CreateProductDto("updated_pro1"),
                new CreateProductDto("updated_pro2"),
                new CreateProductDto("updated_pro3")
        );
        var updateRecipeDto = new UpdateRecipeDto("recipe1_updated", "recipe1_desc_updated", Difficulty.HARD, 2000d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "updateRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(updateRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipe/{id}", savedRecipeIds.get(4))
                        .file(recipeData)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("test_user")
    void updateRecipeShouldReturnNotFound() throws Exception {
        var createProductsDto = List.of(
                new CreateProductDto("updated_pro1"),
                new CreateProductDto("updated_pro2"),
                new CreateProductDto("updated_pro3")
        );
        var updateRecipeDto = new UpdateRecipeDto("recipe1_updated", "recipe1_desc_updated", Difficulty.HARD, 2000d, createProductsDto);

        MockMultipartFile recipeData = new MockMultipartFile(
                "updateRecipeDto",
                "",
                "application/json",
                objectMapper.writeValueAsString(updateRecipeDto).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/recipe/{id}", savedRecipeIds.get(0) + 100l)
                        .file(recipeData)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());
    }


    static void seedTestData() {
        User freshUser = staticUserRepository.findById(staticLoggedTestUser.getId()).orElseThrow();
        User unauthorizedUser = staticUserRepository.findById(staticTestUser.getId()).orElseThrow();

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
                new Recipe(null, "recipe1", "recipe1_desc", Difficulty.EASY, 200d,null, null, List.of(product1, product2), null, freshUser),
                new Recipe(null, "recipe2", "recipe2_desc", Difficulty.MEDIUM, 300d,null, null, List.of(product1, product2, product3), null, freshUser),
                new Recipe(null, "recipe3", "recipe3_desc", Difficulty.MEDIUM, 250d,null, null, List.of(product2), null, freshUser),
                new Recipe(null, "recipe4", "recipe4_desc", Difficulty.HARD, 600d,null, null, List.of(product4, product2, product1), null, freshUser),
                new Recipe(null, "recipe5", "recipe5_desc", Difficulty.EASY, 150d,null, null, List.of(product3, product4), null, unauthorizedUser)
        );

        recipes = staticRecipeRepository.saveAllAndFlush(recipes);

        recipes.stream().forEach(recipe -> {savedRecipeIds.add(recipe.getId());});
    }

}
