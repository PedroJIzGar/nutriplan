package com.nutriplan.api.features.recipes.dto;

import java.math.BigDecimal;
import java.util.List;

import com.nutriplan.api.features.recipes.domain.enums.MealType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateRecipeRequest {

    @NotBlank(message = "Recipe name is required")
    @Size(max = 150, message = "Recipe name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotNull(message = "Servings are required")
    @Positive(message = "Servings must be greater than 0")
    private Integer servings;

    @NotNull(message = "Target kcal is required")
    @DecimalMin(value = "0.01", message = "Target kcal must be greater than 0")
    private BigDecimal targetKcal;

    @NotNull(message = "Protein is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Protein must be greater than or equal to 0")
    private BigDecimal protein;

    @NotNull(message = "Carbs are required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Carbs must be greater than or equal to 0")
    private BigDecimal carbs;

    @NotNull(message = "Fat is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fat must be greater than or equal to 0")
    private BigDecimal fat;

    @NotNull(message = "Preparation time is required")
    @Positive(message = "Preparation time must be greater than 0")
    private Integer prepTimeMinutes;

    @NotEmpty(message = "Recipe must contain at least one ingredient")
    @Valid
    private List<CreateRecipeIngredientRequest> ingredients;
}