package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.entities.Product;
import com.springtest.cookapi.domain.mappers.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductMapperTest {
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    void shouldMapCreateProductDtoToEntity() {
        CreateProductDto dto = new CreateProductDto("Test Product");

        Product entity = productMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Test Product");
        assertThat(entity.getRecipes()).isNotNull().isEmpty();
    }

    @Test
    void shouldMapProductDtoToEntity() {
        ProductDto dto = new ProductDto(1L, "Existing Product");

        Product entity = productMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Existing Product");
        assertThat(entity.getRecipes()).isNotNull().isEmpty();
    }

    @Test
    void shouldMapProductEntityToDto() {
        Product entity = new Product(1L, "Test Product", new ArrayList<>());

        ProductDto dto = productMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Test Product");
    }

    @Test
    void shouldHandleNullNameInCreateDto() {
        CreateProductDto dto = new CreateProductDto(null);

        Product entity = productMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isNull();
        assertThat(entity.getRecipes()).isNotNull().isEmpty();
    }

    @Test
    void shouldHandleEmptyNameInDto() {
        CreateProductDto dto = new CreateProductDto("");

        Product entity = productMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Product1", "Very Long Product Name", "Product-With-Dashes", "Продукт"})
    void shouldMapDifferentProductNames(String productName) {
        // Given
        CreateProductDto dto = new CreateProductDto(productName);

        // When
        Product entity = productMapper.toEntity(dto);

        // Then
        assertThat(entity.getName()).isEqualTo(productName);
    }
}
