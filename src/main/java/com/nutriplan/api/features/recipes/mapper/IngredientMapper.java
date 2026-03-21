package com.nutriplan.api.features.recipes.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nutriplan.api.features.recipes.domain.Ingredient;
import com.nutriplan.api.features.recipes.dto.IngredientResponse;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientResponse toResponse(Ingredient ingredient);

    List<IngredientResponse> toResponseList(List<Ingredient> ingredients);
}