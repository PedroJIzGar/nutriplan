package com.nutriplan.api.features.weeklyplans.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
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
public class CreateWeeklyPlanRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}