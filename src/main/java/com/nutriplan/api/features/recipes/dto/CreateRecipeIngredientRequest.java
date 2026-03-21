package com.nutriplan.api.features.recipes.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateRecipeIngredientRequest {

    @NotNull(message = "Ingredient id is required")
    private UUID ingredientId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private IngredientUnit unit;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}