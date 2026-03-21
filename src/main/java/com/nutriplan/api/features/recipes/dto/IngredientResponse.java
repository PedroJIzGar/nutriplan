package com.nutriplan.api.features.recipes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.enums.IngredientCategory;
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
public class IngredientResponse {

    private UUID id;
    private String name;
    private IngredientUnit defaultUnit;
    private IngredientCategory category;
    private OffsetDateTime createdAt;
}