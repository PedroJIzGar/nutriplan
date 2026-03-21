package com.nutriplan.api.features.weeklyplans.services;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.recipes.domain.Recipe;
import com.nutriplan.api.features.recipes.domain.repository.RecipeRepository;
import com.nutriplan.api.features.weeklyplans.domain.PlannedMeal;
import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;
import com.nutriplan.api.features.weeklyplans.domain.repository.PlannedMealRepository;
import com.nutriplan.api.features.weeklyplans.domain.repository.WeeklyPlanRepository;
import com.nutriplan.api.features.weeklyplans.dto.CreatePlannedMealRequest;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;
import com.nutriplan.api.features.weeklyplans.mapper.PlannedMealMapper;
import com.nutriplan.api.shared.exception.BadRequestException;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlannedMealService {

        private static final String DUPLICATE_SLOT_MESSAGE = "A planned meal already exists for this day and meal type";

        private final PlannedMealRepository plannedMealRepository;
        private final WeeklyPlanRepository weeklyPlanRepository;
        private final RecipeRepository recipeRepository;
        private final PlannedMealMapper plannedMealMapper;

        @Transactional
        public PlannedMealResponse addMeal(UUID planId, CreatePlannedMealRequest request) {
                UUID userId = SecurityUtils.getCurrentUserId();

                WeeklyPlan weeklyPlan = weeklyPlanRepository.findByIdAndUserId(planId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Weekly plan not found"));

                Recipe recipe = recipeRepository.findById(request.getRecipeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

                if (!Boolean.TRUE.equals(recipe.getActive())) {
                        throw new BadRequestException("Recipe is inactive");
                }

                if (!recipe.getMealType().equals(request.getMealType())) {
                        throw new BadRequestException("Recipe meal type does not match requested meal type");
                }

                boolean slotAlreadyUsed = plannedMealRepository.existsByWeeklyPlanIdAndDayOfWeekAndMealType(
                                weeklyPlan.getId(),
                                request.getDayOfWeek(),
                                request.getMealType());

                if (slotAlreadyUsed) {
                        throw new ConflictException(DUPLICATE_SLOT_MESSAGE);
                }

                PlannedMeal plannedMeal = PlannedMeal.builder()
                                .weeklyPlan(weeklyPlan)
                                .recipe(recipe)
                                .dayOfWeek(request.getDayOfWeek())
                                .mealType(request.getMealType())
                                .build();

                try {
                        PlannedMeal savedPlannedMeal = plannedMealRepository.save(plannedMeal);
                        return plannedMealMapper.toResponse(savedPlannedMeal);
                } catch (DataIntegrityViolationException ex) {
                        throw new ConflictException(DUPLICATE_SLOT_MESSAGE);
                }
        }

        public List<PlannedMealResponse> getMealsByPlanId(UUID planId) {
                UUID userId = SecurityUtils.getCurrentUserId();

                WeeklyPlan weeklyPlan = weeklyPlanRepository.findByIdAndUserId(planId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Weekly plan not found"));

                return plannedMealRepository.findByWeeklyPlanIdOrderByDayOfWeekAscMealTypeAsc(weeklyPlan.getId())
                                .stream()
                                .map(plannedMealMapper::toResponse)
                                .toList();
        }
}