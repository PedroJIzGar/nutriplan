package com.nutriplan.api.features.recipes.dto;

import com.nutriplan.api.features.recipes.domain.enums.IngredientCategory;
import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;

import jakarta.validation.constraints.NotBlank;
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
public class CreateIngredientRequest {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Default unit is required")
    private IngredientUnit defaultUnit;

    @NotNull(message = "Ingredient category is required")
    private IngredientCategory category;
}