package com.nutriplan.api.features.weeklyplans.dto;

import java.time.DayOfWeek;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.enums.MealType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlannedMealRequest {

    @NotNull(message = "Recipe id is required")
    private UUID recipeId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Meal type is required")
    private MealType mealType;
}