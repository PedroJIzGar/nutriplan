package com.nutriplan.api.feature.weeklyplans.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.nutriplan.api.features.recipes.domain.Recipe;
import com.nutriplan.api.features.recipes.domain.enums.MealType;
import com.nutriplan.api.features.recipes.domain.repository.RecipeRepository;
import com.nutriplan.api.features.weeklyplans.domain.PlannedMeal;
import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;
import com.nutriplan.api.features.weeklyplans.domain.repository.PlannedMealRepository;
import com.nutriplan.api.features.weeklyplans.domain.repository.WeeklyPlanRepository;
import com.nutriplan.api.features.weeklyplans.dto.CreatePlannedMealRequest;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;
import com.nutriplan.api.features.weeklyplans.mapper.PlannedMealMapper;
import com.nutriplan.api.features.weeklyplans.services.PlannedMealService;
import com.nutriplan.api.shared.exception.BadRequestException;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

class PlannedMealServiceTest {

    private final PlannedMealRepository plannedMealRepository = Mockito.mock(PlannedMealRepository.class);
    private final WeeklyPlanRepository weeklyPlanRepository = Mockito.mock(WeeklyPlanRepository.class);
    private final RecipeRepository recipeRepository = Mockito.mock(RecipeRepository.class);
    private final PlannedMealMapper plannedMealMapper = Mockito.mock(PlannedMealMapper.class);

    private final PlannedMealService plannedMealService = new PlannedMealService(
            plannedMealRepository,
            weeklyPlanRepository,
            recipeRepository,
            plannedMealMapper);

    @Test
    @DisplayName("addMeal should save planned meal when request is valid")
    void addMealShouldSavePlannedMeal() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Overnight Oats")
                .mealType(MealType.BREAKFAST)
                .targetKcal(BigDecimal.valueOf(350))
                .protein(BigDecimal.valueOf(20))
                .carbs(BigDecimal.valueOf(40))
                .fat(BigDecimal.valueOf(10))
                .active(true)
                .build();

        PlannedMeal savedPlannedMeal = PlannedMeal.builder()
                .id(UUID.randomUUID())
                .weeklyPlan(weeklyPlan)
                .recipe(recipe)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:29Z"))
                .build();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(recipeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        PlannedMealResponse response = PlannedMealResponse.builder()
                .id(savedPlannedMeal.getId())
                .weeklyPlanId(planId)
                .recipeId(recipeId)
                .recipeName("Overnight Oats")
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .createdAt(savedPlannedMeal.getCreatedAt())
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(plannedMealRepository.existsByWeeklyPlanIdAndDayOfWeekAndMealType(planId, DayOfWeek.MONDAY,
                    MealType.BREAKFAST))
                    .thenReturn(false);
            when(plannedMealRepository.save(any(PlannedMeal.class))).thenReturn(savedPlannedMeal);
            when(plannedMealMapper.toResponse(savedPlannedMeal)).thenReturn(response);

            PlannedMealResponse result = plannedMealService.addMeal(planId, request);

            assertNotNull(result);
            assertEquals(recipeId, result.getRecipeId());
            assertEquals("Overnight Oats", result.getRecipeName());
        }
    }

    @Test
    @DisplayName("addMeal should throw not found when weekly plan does not exist")
    void addMealShouldThrowNotFoundWhenPlanDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(UUID.randomUUID())
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> plannedMealService.addMeal(planId, request));
        }
    }

    @Test
    @DisplayName("addMeal should throw not found when recipe does not exist")
    void addMealShouldThrowNotFoundWhenRecipeDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(recipeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> plannedMealService.addMeal(planId, request));
        }
    }

    @Test
    @DisplayName("addMeal should throw bad request when recipe is inactive")
    void addMealShouldThrowBadRequestWhenRecipeIsInactive() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Old Recipe")
                .mealType(MealType.BREAKFAST)
                .active(false)
                .build();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(recipeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

            assertThrows(BadRequestException.class, () -> plannedMealService.addMeal(planId, request));
        }
    }

    @Test
    @DisplayName("addMeal should throw bad request when recipe meal type does not match")
    void addMealShouldThrowBadRequestWhenMealTypeDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Chicken Bowl")
                .mealType(MealType.LUNCH)
                .active(true)
                .build();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(recipeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

            assertThrows(BadRequestException.class, () -> plannedMealService.addMeal(planId, request));
        }
    }

    @Test
    @DisplayName("addMeal should throw conflict when slot is already used")
    void addMealShouldThrowConflictWhenSlotAlreadyUsed() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Overnight Oats")
                .mealType(MealType.BREAKFAST)
                .active(true)
                .build();

        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder()
                .recipeId(recipeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
            when(plannedMealRepository.existsByWeeklyPlanIdAndDayOfWeekAndMealType(planId, DayOfWeek.MONDAY,
                    MealType.BREAKFAST))
                    .thenReturn(true);

            assertThrows(ConflictException.class, () -> plannedMealService.addMeal(planId, request));
        }
    }

    @Test
    @DisplayName("getMealsByPlanId should return meals for current user's plan")
    void getMealsByPlanIdShouldReturnMeals() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .build();

        PlannedMeal plannedMeal = PlannedMeal.builder()
                .id(UUID.randomUUID())
                .weeklyPlan(weeklyPlan)
                .recipe(Recipe.builder()
                        .id(UUID.randomUUID())
                        .name("Banana and Yogurt Cup")
                        .mealType(MealType.BREAKFAST)
                        .active(true)
                        .build())
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        PlannedMealResponse response = PlannedMealResponse.builder()
                .id(plannedMeal.getId())
                .weeklyPlanId(planId)
                .recipeName("Banana and Yogurt Cup")
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(weeklyPlan));
            when(plannedMealRepository.findByWeeklyPlanIdOrderByDayOfWeekAscMealTypeAsc(planId))
                    .thenReturn(List.of(plannedMeal));
            when(plannedMealMapper.toResponse(plannedMeal)).thenReturn(response);

            List<PlannedMealResponse> result = plannedMealService.getMealsByPlanId(planId);

            assertEquals(1, result.size());
            assertEquals("Banana and Yogurt Cup", result.get(0).getRecipeName());
        }
    }
}