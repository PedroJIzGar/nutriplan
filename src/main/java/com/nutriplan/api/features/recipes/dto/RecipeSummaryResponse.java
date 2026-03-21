package com.nutriplan.api.features.recipes.dto;

import java.math.BigDecimal;
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
public class RecipeSummaryResponse {

    private UUID id;
    private String name;
    private String description;
    private MealType mealType;
    private Integer servings;
    private BigDecimal targetKcal;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Integer prepTimeMinutes;
    private Boolean active;
    private OffsetDateTime createdAt;
}