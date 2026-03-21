package com.nutriplan.api.features.weeklyplans.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;
import com.nutriplan.api.features.weeklyplans.domain.repository.WeeklyPlanRepository;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;
import com.nutriplan.api.features.weeklyplans.dto.CreateWeeklyPlanRequest;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanDetailResponse;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanResponse;
import com.nutriplan.api.features.weeklyplans.mapper.PlannedMealMapper;
import com.nutriplan.api.features.weeklyplans.mapper.WeeklyPlanMapper;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyPlanService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final WeeklyPlanMapper weeklyPlanMapper;
    private final PlannedMealMapper plannedMealMapper;

    @Transactional
    public WeeklyPlanResponse create(CreateWeeklyPlanRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateStartDateIsStartOfWeek(request.getStartDate());

        if (weeklyPlanRepository.existsByUserIdAndStartDate(userId, request.getStartDate())) {
            throw new ConflictException("A weekly plan already exists for this start date");
        }

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .userId(userId)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusDays(6))
                .build();

        WeeklyPlan savedWeeklyPlan = weeklyPlanRepository.save(weeklyPlan);

        return weeklyPlanMapper.toResponse(savedWeeklyPlan);
    }

    public List<WeeklyPlanResponse> getMyPlans() {
        UUID userId = SecurityUtils.getCurrentUserId();

        List<WeeklyPlan> weeklyPlans = weeklyPlanRepository.findAllByUserIdOrderByStartDateDesc(userId);

        return weeklyPlanMapper.toResponseList(weeklyPlans);
    }

    public WeeklyPlanDetailResponse getById(UUID planId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        WeeklyPlan weeklyPlan = weeklyPlanRepository.findDetailedByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly plan not found"));

        List<PlannedMealResponse> plannedMeals = weeklyPlan.getPlannedMeals()
                .stream()
                .sorted(Comparator
                        .comparing((com.nutriplan.api.features.weeklyplans.domain.PlannedMeal pm) -> pm.getDayOfWeek()
                                .getValue())
                        .thenComparing(pm -> pm.getMealType().name()))
                .map(plannedMealMapper::toResponse)
                .toList();

        return WeeklyPlanDetailResponse.builder()
                .id(weeklyPlan.getId())
                .startDate(weeklyPlan.getStartDate())
                .endDate(weeklyPlan.getEndDate())
                .createdAt(weeklyPlan.getCreatedAt())
                .plannedMeals(plannedMeals)
                .build();
    }

    private void validateStartDateIsStartOfWeek(LocalDate startDate) {
        if (startDate == null || !DayOfWeek.MONDAY.equals(startDate.getDayOfWeek())) {
            throw new ConflictException("Start date must be a Monday");
        }
    }
}