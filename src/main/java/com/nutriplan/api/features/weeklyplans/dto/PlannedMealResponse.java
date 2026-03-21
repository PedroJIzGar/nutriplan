package com.nutriplan.api.features.weeklyplans.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.enums.MealType;

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
public class PlannedMealResponse {

        private UUID id;
        private UUID weeklyPlanId;
        private UUID recipeId;
        private String recipeName;
        private DayOfWeek dayOfWeek;
        private MealType mealType;
        private BigDecimal targetKcal;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
        private OffsetDateTime createdAt;
}