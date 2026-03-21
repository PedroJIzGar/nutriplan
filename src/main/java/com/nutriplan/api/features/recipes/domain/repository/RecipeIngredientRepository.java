package com.nutriplan.api.features.recipes.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutriplan.api.features.recipes.domain.RecipeIngredient;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, UUID> {
}