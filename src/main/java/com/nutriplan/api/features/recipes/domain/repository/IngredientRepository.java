package com.nutriplan.api.features.recipes.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutriplan.api.features.recipes.domain.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<Ingredient> findAllByOrderByNameAsc();
}