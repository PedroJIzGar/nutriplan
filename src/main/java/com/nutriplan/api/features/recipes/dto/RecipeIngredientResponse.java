package com.nutriplan.api.features.recipes.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;

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
public class RecipeIngredientResponse {

    private UUID id;
    private UUID ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private IngredientUnit unit;
    private String notes;
}