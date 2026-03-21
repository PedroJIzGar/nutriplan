package com.nutriplan.api.features.weeklyplans.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
public class WeeklyPlanResponse {

    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime createdAt;
}