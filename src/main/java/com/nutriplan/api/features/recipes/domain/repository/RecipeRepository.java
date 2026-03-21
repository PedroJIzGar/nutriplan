package com.nutriplan.api.features.recipes.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutriplan.api.features.recipes.domain.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findAllByOrderByCreatedAtDesc();

    List<Recipe> findAllByActiveTrueOrderByCreatedAtDesc();
}