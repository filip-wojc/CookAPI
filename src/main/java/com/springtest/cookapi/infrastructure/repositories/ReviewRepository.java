package com.springtest.cookapi.infrastructure.repositories;

import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> getReviewByRecipe(Recipe recipe);
}
