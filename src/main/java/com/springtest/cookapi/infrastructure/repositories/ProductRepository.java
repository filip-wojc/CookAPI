package com.springtest.cookapi.infrastructure.repositories;

import com.springtest.cookapi.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product, Long> {

}
