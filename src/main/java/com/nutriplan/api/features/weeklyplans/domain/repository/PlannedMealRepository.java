package com.nutriplan.api.features.weeklyplans.domain.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutriplan.api.features.recipes.domain.enums.MealType;
import com.nutriplan.api.features.weeklyplans.domain.PlannedMeal;

public interface PlannedMealRepository extends JpaRepository<PlannedMeal, UUID> {

    List<PlannedMeal> findByWeeklyPlanIdOrderByDayOfWeekAscMealTypeAsc(UUID weeklyPlanId);

    boolean existsByWeeklyPlanIdAndDayOfWeekAndMealType(
            UUID weeklyPlanId,
            DayOfWeek dayOfWeek,
            MealType mealType);
}