package com.nutriplan.api.features.planninggroups.dto;

import java.util.UUID;

import com.nutriplan.api.features.planninggroups.domain.enums.PlanningGroupRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningGroupMemberResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private PlanningGroupRole role;
    private Double portionFactor;
}