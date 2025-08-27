package com.springtest.cookapi.infrastructure.repositories;

import com.springtest.cookapi.domain.entities.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {

}
