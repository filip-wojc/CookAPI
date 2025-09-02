package com.springtest.cookapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.recipe.CreateRecipeDto;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Difficulty;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
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

    static UserRepository staticUserRepository;
    static User staticTestUser;

    @BeforeAll
    static void init(@Autowired UserRepository userRepository) {
        staticUserRepository = userRepository;

        staticTestUser = new User("test","test_user", "test_password", Role.USER);
        staticTestUser = staticUserRepository.save(staticTestUser);
    }

    @Test
    void connectionEstablished() {
        Assertions.assertThat(postgreSQLContainer.isCreated()).isTrue();
        Assertions.assertThat(postgreSQLContainer.isRunning()).isTrue();

        Assertions.assertThat(redisContainer.isCreated()).isTrue();
        Assertions.assertThat(redisContainer.isRunning()).isTrue();
    }

    @Test
    @WithUserDetails("test_user")
    void addRecipeShouldReturnOk() throws Exception {

        var createProductsDto = List.of(
                new CreateProductDto("test_pro1"),
                new CreateProductDto("test_pro2"),
                new CreateProductDto("test_pro3")
        );
        var createRecipeDto = new CreateRecipeDto("test1", "test_d1", Difficulty.MEDIUM, 200d, createProductsDto);
        mockMvc.perform(post("/api/recipe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRecipeDto)))
                .andExpect(status().isOk());
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

        mockMvc.perform(post("/api/recipe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRecipeDto)))
                .andExpect(status().isBadRequest());
    }
}
