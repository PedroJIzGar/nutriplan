package com.nutriplan.api.features.planninggroups.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPlanningGroupMemberRequest {

    @NotNull(message = "El usuario es obligatorio")
    private UUID userId;

    @NotNull(message = "El factor de ración es obligatorio")
    @Positive(message = "El factor de ración debe ser mayor que 0")
    private Double portionFactor;
}