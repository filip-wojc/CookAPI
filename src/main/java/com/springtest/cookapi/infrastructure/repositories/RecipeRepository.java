package com.springtest.cookapi.infrastructure.repositories;

import com.springtest.cookapi.domain.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {

}
