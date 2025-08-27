package com.springtest.cookapi.domain.mappers;

import com.springtest.cookapi.domain.dtos.product.CreateProductDto;
import com.springtest.cookapi.domain.dtos.product.ProductDto;
import com.springtest.cookapi.domain.entities.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProductMapper {
    public Product toEntity(CreateProductDto dto) {
        return new Product(
                null,
                dto.name(),
                new ArrayList<>()
        );
    }

    public ProductDto toDto(Product entity) {
        return new ProductDto(
                entity.getId(),
                entity.getName()
        );
    }
}
