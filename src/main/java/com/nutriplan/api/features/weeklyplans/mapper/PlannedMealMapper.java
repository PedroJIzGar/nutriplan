package com.nutriplan.api.features.weeklyplans.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nutriplan.api.features.weeklyplans.domain.PlannedMeal;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;

@Mapper(componentModel = "spring")
public interface PlannedMealMapper {

    @Mapping(target = "weeklyPlanId", source = "weeklyPlan.id")
    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeName", source = "recipe.name")
    @Mapping(target = "targetKcal", source = "recipe.targetKcal")
    @Mapping(target = "protein", source = "recipe.protein")
    @Mapping(target = "carbs", source = "recipe.carbs")
    @Mapping(target = "fat", source = "recipe.fat")
    PlannedMealResponse toResponse(PlannedMeal plannedMeal);
}