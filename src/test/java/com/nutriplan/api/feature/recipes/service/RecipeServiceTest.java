package com.nutriplan.api.feature.recipes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.Ingredient;
import com.nutriplan.api.features.recipes.domain.Recipe;
import com.nutriplan.api.features.recipes.domain.enums.IngredientCategory;
import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;
import com.nutriplan.api.features.recipes.domain.enums.MealType;
import com.nutriplan.api.features.recipes.domain.repository.IngredientRepository;
import com.nutriplan.api.features.recipes.domain.repository.RecipeRepository;
import com.nutriplan.api.features.recipes.dto.CreateRecipeIngredientRequest;
import com.nutriplan.api.features.recipes.dto.CreateRecipeRequest;
import com.nutriplan.api.features.recipes.dto.RecipeResponse;
import com.nutriplan.api.features.recipes.mapper.RecipeMapper;
import com.nutriplan.api.features.recipes.services.RecipeService;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("createRecipe should create recipe when request is valid")
    void createRecipeShouldCreateRecipeWhenRequestIsValid() {
        UUID chickenId = UUID.randomUUID();
        UUID riceId = UUID.randomUUID();

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .name("  Chicken Rice Bowl  ")
                .description("  High protein lunch  ")
                .mealType(MealType.LUNCH)
                .servings(2)
                .targetKcal(new BigDecimal("650.00"))
                .protein(new BigDecimal("45.00"))
                .carbs(new BigDecimal("60.00"))
                .fat(new BigDecimal("15.00"))
                .prepTimeMinutes(20)
                .ingredients(List.of(
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(chickenId)
                                .quantity(new BigDecimal("200.00"))
                                .unit(IngredientUnit.GRAM)
                                .notes("  grilled ")
                                .build(),
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(riceId)
                                .quantity(new BigDecimal("150.00"))
                                .unit(IngredientUnit.GRAM)
                                .notes(" ")
                                .build()))
                .build();

        Ingredient chicken = Ingredient.builder()
                .id(chickenId)
                .name("Chicken")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.MEAT)
                .build();

        Ingredient rice = Ingredient.builder()
                .id(riceId)
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id(UUID.randomUUID())
                .name("Chicken Rice Bowl")
                .mealType(MealType.LUNCH)
                .active(true)
                .build();

        RecipeResponse response = RecipeResponse.builder()
                .id(savedRecipe.getId())
                .name(savedRecipe.getName())
                .mealType(savedRecipe.getMealType())
                .active(true)
                .build();

        when(ingredientRepository.findAllById(List.of(chickenId, riceId))).thenReturn(List.of(chicken, rice));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);
        when(recipeMapper.toResponse(savedRecipe)).thenReturn(response);

        RecipeResponse result = recipeService.createRecipe(request);

        assertThat(result).isEqualTo(response);

        verify(ingredientRepository).findAllById(List.of(chickenId, riceId));
        verify(recipeRepository).save(argThat(matchesRecipeBuiltFromRequest()));
        verify(recipeMapper).toResponse(savedRecipe);
    }

    @Test
    @DisplayName("createRecipe should throw conflict when request contains duplicated ingredients")
    void createRecipeShouldThrowConflictWhenRequestContainsDuplicatedIngredients() {
        UUID ingredientId = UUID.randomUUID();

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .name("Omelette")
                .mealType(MealType.DINNER)
                .servings(1)
                .targetKcal(new BigDecimal("300.00"))
                .protein(new BigDecimal("20.00"))
                .carbs(new BigDecimal("5.00"))
                .fat(new BigDecimal("18.00"))
                .prepTimeMinutes(10)
                .ingredients(List.of(
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(ingredientId)
                                .quantity(new BigDecimal("2.00"))
                                .unit(IngredientUnit.UNIT)
                                .build(),
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(ingredientId)
                                .quantity(new BigDecimal("1.00"))
                                .unit(IngredientUnit.UNIT)
                                .build()))
                .build();

        assertThrows(ConflictException.class, () -> recipeService.createRecipe(request));

        verify(ingredientRepository, never()).findAllById(any());
        verify(recipeRepository, never()).save(any());
        verify(recipeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("createRecipe should throw not found when one or more ingredients do not exist")
    void createRecipeShouldThrowNotFoundWhenOneOrMoreIngredientsDoNotExist() {
        UUID chickenId = UUID.randomUUID();
        UUID riceId = UUID.randomUUID();

        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .name("Chicken Rice Bowl")
                .mealType(MealType.LUNCH)
                .servings(2)
                .targetKcal(new BigDecimal("650.00"))
                .protein(new BigDecimal("45.00"))
                .carbs(new BigDecimal("60.00"))
                .fat(new BigDecimal("15.00"))
                .prepTimeMinutes(20)
                .ingredients(List.of(
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(chickenId)
                                .quantity(new BigDecimal("200.00"))
                                .unit(IngredientUnit.GRAM)
                                .build(),
                        CreateRecipeIngredientRequest.builder()
                                .ingredientId(riceId)
                                .quantity(new BigDecimal("150.00"))
                                .unit(IngredientUnit.GRAM)
                                .build()))
                .build();

        Ingredient onlyChicken = Ingredient.builder()
                .id(chickenId)
                .name("Chicken")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.MEAT)
                .build();

        when(ingredientRepository.findAllById(List.of(chickenId, riceId))).thenReturn(List.of(onlyChicken));

        assertThrows(ResourceNotFoundException.class, () -> recipeService.createRecipe(request));

        verify(ingredientRepository).findAllById(List.of(chickenId, riceId));
        verify(recipeRepository, never()).save(any());
        verify(recipeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("getAllRecipes should return mapped recipe list")
    void getAllRecipesShouldReturnMappedRecipeList() {
        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .name("Porridge")
                .mealType(MealType.BREAKFAST)
                .active(true)
                .build();

        RecipeResponse response = RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .mealType(recipe.getMealType())
                .active(true)
                .build();

        when(recipeRepository.findAllByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(recipe));
        when(recipeMapper.toResponseList(List.of(recipe))).thenReturn(List.of(response));

        List<RecipeResponse> result = recipeService.getAllRecipes();

        assertThat(result).containsExactly(response);

        verify(recipeRepository).findAllByActiveTrueOrderByCreatedAtDesc();
        verify(recipeMapper).toResponseList(List.of(recipe));
    }

    @Test
    @DisplayName("getRecipeById should return recipe when found and active")
    void getRecipeByIdShouldReturnRecipeWhenFoundAndActive() {
        UUID recipeId = UUID.randomUUID();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Porridge")
                .mealType(MealType.BREAKFAST)
                .active(true)
                .build();

        RecipeResponse response = RecipeResponse.builder()
                .id(recipeId)
                .name("Porridge")
                .mealType(MealType.BREAKFAST)
                .active(true)
                .build();

        when(recipeRepository.findByIdAndActiveTrue(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(response);

        RecipeResponse result = recipeService.getRecipeById(recipeId);

        assertThat(result).isEqualTo(response);

        verify(recipeRepository).findByIdAndActiveTrue(recipeId);
        verify(recipeMapper).toResponse(recipe);
    }

    @Test
    @DisplayName("getRecipeById should throw not found when recipe does not exist or is inactive")
    void getRecipeByIdShouldThrowNotFoundWhenRecipeDoesNotExistOrIsInactive() {
        UUID recipeId = UUID.randomUUID();

        when(recipeRepository.findByIdAndActiveTrue(recipeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeById(recipeId));

        verify(recipeRepository).findByIdAndActiveTrue(recipeId);
        verify(recipeMapper, never()).toResponse(any());
    }

    private ArgumentMatcher<Recipe> matchesRecipeBuiltFromRequest() {
        return recipe -> recipe.getName().equals("Chicken Rice Bowl")
                && recipe.getDescription().equals("High protein lunch")
                && recipe.getMealType() == MealType.LUNCH
                && recipe.getServings().equals(2)
                && recipe.getTargetKcal().compareTo(new BigDecimal("650.00")) == 0
                && recipe.getProtein().compareTo(new BigDecimal("45.00")) == 0
                && recipe.getCarbs().compareTo(new BigDecimal("60.00")) == 0
                && recipe.getFat().compareTo(new BigDecimal("15.00")) == 0
                && recipe.getPrepTimeMinutes().equals(20)
                && Boolean.TRUE.equals(recipe.getActive())
                && recipe.getIngredients().size() == 2
                && recipe.getIngredients().stream().allMatch(ri -> ri.getRecipe() == recipe);
    }
}