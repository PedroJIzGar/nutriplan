package com.nutriplan.api.features.weeklyplans.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanResponse;

@Mapper(componentModel = "spring")
public interface WeeklyPlanMapper {

    WeeklyPlanResponse toResponse(WeeklyPlan weeklyPlan);

    List<WeeklyPlanResponse> toResponseList(List<WeeklyPlan> weeklyPlans);
}