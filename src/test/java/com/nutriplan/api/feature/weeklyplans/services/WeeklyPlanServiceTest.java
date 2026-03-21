package com.nutriplan.api.feature.weeklyplans.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;
import com.nutriplan.api.features.weeklyplans.domain.repository.WeeklyPlanRepository;
import com.nutriplan.api.features.weeklyplans.dto.CreateWeeklyPlanRequest;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanDetailResponse;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanResponse;
import com.nutriplan.api.features.weeklyplans.mapper.PlannedMealMapper;
import com.nutriplan.api.features.weeklyplans.mapper.WeeklyPlanMapper;
import com.nutriplan.api.features.weeklyplans.services.WeeklyPlanService;
import com.nutriplan.api.shared.exception.BadRequestException;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.security.CurrentUserProfileGuard;
import com.nutriplan.api.shared.utils.SecurityUtils;

class WeeklyPlanServiceTest {

    private final WeeklyPlanRepository weeklyPlanRepository = Mockito.mock(WeeklyPlanRepository.class);
    private final WeeklyPlanMapper weeklyPlanMapper = Mockito.mock(WeeklyPlanMapper.class);
    private final PlannedMealMapper plannedMealMapper = Mockito.mock(PlannedMealMapper.class);
    private final CurrentUserProfileGuard currentUserProfileGuard = Mockito.mock(CurrentUserProfileGuard.class);

    private final WeeklyPlanService weeklyPlanService = new WeeklyPlanService(
            weeklyPlanRepository,
            weeklyPlanMapper,
            plannedMealMapper,
            currentUserProfileGuard);

    @Test
    @DisplayName("create should save weekly plan when request is valid")
    void createShouldSaveWeeklyPlan() {
        UUID userId = UUID.randomUUID();
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .build();

        WeeklyPlan savedEntity = WeeklyPlan.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 29))
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:00Z"))
                .build();

        WeeklyPlanResponse response = WeeklyPlanResponse.builder()
                .id(savedEntity.getId())
                .startDate(savedEntity.getStartDate())
                .endDate(savedEntity.getEndDate())
                .createdAt(savedEntity.getCreatedAt())
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.existsByUserIdAndStartDate(userId, request.getStartDate())).thenReturn(false);
            when(weeklyPlanRepository.save(any(WeeklyPlan.class))).thenReturn(savedEntity);
            when(weeklyPlanMapper.toResponse(savedEntity)).thenReturn(response);

            WeeklyPlanResponse result = weeklyPlanService.create(request);

            assertNotNull(result);
            assertEquals(LocalDate.of(2026, 3, 29), result.getEndDate());
            verify(currentUserProfileGuard).ensureExists(userId);
            verify(weeklyPlanRepository).save(any(WeeklyPlan.class));
        }
    }

    @Test
    @DisplayName("create should throw conflict when weekly plan already exists")
    void createShouldThrowConflictWhenPlanAlreadyExists() {
        UUID userId = UUID.randomUUID();
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.existsByUserIdAndStartDate(userId, request.getStartDate())).thenReturn(true);

            assertThrows(ConflictException.class, () -> weeklyPlanService.create(request));
        }
    }

    @Test
    @DisplayName("create should throw bad request when start date is not monday")
    void createShouldThrowBadRequestWhenStartDateIsNotMonday() {
        UUID userId = UUID.randomUUID();
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 24))
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            assertThrows(BadRequestException.class, () -> weeklyPlanService.create(request));
        }
    }

    @Test
    @DisplayName("getMyPlans should return current user plans")
    void getMyPlansShouldReturnCurrentUserPlans() {
        UUID userId = UUID.randomUUID();

        WeeklyPlan entity = WeeklyPlan.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 29))
                .build();

        WeeklyPlanResponse response = WeeklyPlanResponse.builder()
                .id(entity.getId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findAllByUserIdOrderByStartDateDesc(userId)).thenReturn(List.of(entity));
            when(weeklyPlanMapper.toResponseList(List.of(entity))).thenReturn(List.of(response));

            List<WeeklyPlanResponse> result = weeklyPlanService.getMyPlans();

            assertEquals(1, result.size());
            assertEquals(entity.getId(), result.get(0).getId());
        }
    }

    @Test
    @DisplayName("getById should throw not found when plan does not belong to user")
    void getByIdShouldThrowNotFound() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findDetailedByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> weeklyPlanService.getById(planId));
        }
    }

    @Test
    @DisplayName("getById should return plan detail when it exists")
    void getByIdShouldReturnPlanDetail() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        WeeklyPlan weeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .userId(userId)
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 29))
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:00Z"))
                .plannedMeals(List.of())
                .build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(weeklyPlanRepository.findDetailedByIdAndUserId(planId, userId))
                    .thenReturn(Optional.of(weeklyPlan));

            WeeklyPlanDetailResponse result = weeklyPlanService.getById(planId);

            assertEquals(planId, result.getId());
            assertEquals(LocalDate.of(2026, 3, 23), result.getStartDate());
            assertEquals(LocalDate.of(2026, 3, 29), result.getEndDate());
        }
    }
}