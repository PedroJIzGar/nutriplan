package com.nutriplan.api.features.weeklyplans.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
public class WeeklyPlanDetailResponse {

    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime createdAt;
    private List<PlannedMealResponse> plannedMeals;
}