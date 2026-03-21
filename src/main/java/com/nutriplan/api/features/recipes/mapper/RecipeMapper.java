package com.nutriplan.api.features.recipes.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nutriplan.api.features.recipes.domain.Recipe;
import com.nutriplan.api.features.recipes.domain.RecipeIngredient;
import com.nutriplan.api.features.recipes.dto.RecipeIngredientResponse;
import com.nutriplan.api.features.recipes.dto.RecipeResponse;
import com.nutriplan.api.features.recipes.dto.RecipeSummaryResponse;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    RecipeResponse toResponse(Recipe recipe);

    RecipeSummaryResponse toSummaryResponse(Recipe recipe);

    List<RecipeSummaryResponse> toSummaryResponseList(List<Recipe> recipes);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    RecipeIngredientResponse toIngredientResponse(RecipeIngredient recipeIngredient);

    List<RecipeIngredientResponse> toIngredientResponseList(List<RecipeIngredient> recipeIngredients);
}