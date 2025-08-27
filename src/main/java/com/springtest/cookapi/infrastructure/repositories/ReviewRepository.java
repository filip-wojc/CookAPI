package com.springtest.cookapi.infrastructure.repositories;

import com.springtest.cookapi.domain.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
}
