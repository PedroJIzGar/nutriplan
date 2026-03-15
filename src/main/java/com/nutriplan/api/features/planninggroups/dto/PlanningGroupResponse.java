package com.nutriplan.api.features.planninggroups.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningGroupResponse {

    private UUID id;
    private String name;
    private UUID createdByUserId;
    private OffsetDateTime createdAt;
    private List<PlanningGroupMemberResponse> members;
}