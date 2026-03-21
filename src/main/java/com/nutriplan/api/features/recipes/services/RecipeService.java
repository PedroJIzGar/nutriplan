package com.nutriplan.api.features.recipes.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.recipes.domain.Ingredient;
import com.nutriplan.api.features.recipes.domain.Recipe;
import com.nutriplan.api.features.recipes.domain.RecipeIngredient;
import com.nutriplan.api.features.recipes.domain.repository.IngredientRepository;
import com.nutriplan.api.features.recipes.domain.repository.RecipeRepository;
import com.nutriplan.api.features.recipes.dto.CreateRecipeIngredientRequest;
import com.nutriplan.api.features.recipes.dto.CreateRecipeRequest;
import com.nutriplan.api.features.recipes.dto.RecipeResponse;
import com.nutriplan.api.features.recipes.mapper.RecipeMapper;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;

    public RecipeResponse createRecipe(CreateRecipeRequest request) {
        validateNoDuplicatedIngredients(request.getIngredients());

        Map<UUID, Ingredient> ingredientMap = loadIngredientsAsMap(request.getIngredients());

        Recipe recipe = buildRecipe(request);

        for (CreateRecipeIngredientRequest ingredientRequest : request.getIngredients()) {
            recipe.addIngredient(buildRecipeIngredient(ingredientRequest, ingredientMap));
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getAllRecipes() {
        return recipeMapper.toResponseList(
                recipeRepository.findAllByActiveTrueOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(UUID recipeId) {
        Recipe recipe = recipeRepository.findByIdAndActiveTrue(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        return recipeMapper.toResponse(recipe);
    }

    private Map<UUID, Ingredient> loadIngredientsAsMap(List<CreateRecipeIngredientRequest> ingredientRequests) {
        List<UUID> ingredientIds = ingredientRequests.stream()
                .map(CreateRecipeIngredientRequest::getIngredientId)
                .toList();

        Map<UUID, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        if (ingredientMap.size() != ingredientIds.size()) {
            throw new ResourceNotFoundException("One or more ingredients were not found");
        }

        return ingredientMap;
    }

    private Recipe buildRecipe(CreateRecipeRequest request) {
        return Recipe.builder()
                .name(request.getName().trim())
                .description(normalizeNullableText(request.getDescription()))
                .mealType(request.getMealType())
                .servings(request.getServings())
                .targetKcal(request.getTargetKcal())
                .protein(request.getProtein())
                .carbs(request.getCarbs())
                .fat(request.getFat())
                .prepTimeMinutes(request.getPrepTimeMinutes())
                .active(true)
                .build();
    }

    private RecipeIngredient buildRecipeIngredient(
            CreateRecipeIngredientRequest ingredientRequest,
            Map<UUID, Ingredient> ingredientMap) {
        return RecipeIngredient.builder()
                .ingredient(ingredientMap.get(ingredientRequest.getIngredientId()))
                .quantity(ingredientRequest.getQuantity())
                .unit(ingredientRequest.getUnit())
                .notes(normalizeNullableText(ingredientRequest.getNotes()))
                .build();
    }

    private void validateNoDuplicatedIngredients(List<CreateRecipeIngredientRequest> ingredients) {
        Set<UUID> uniqueIngredientIds = new HashSet<>();

        for (CreateRecipeIngredientRequest ingredient : ingredients) {
            if (!uniqueIngredientIds.add(ingredient.getIngredientId())) {
                throw new ConflictException("Recipe contains duplicated ingredients");
            }
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}