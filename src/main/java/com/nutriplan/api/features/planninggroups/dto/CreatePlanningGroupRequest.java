package com.nutriplan.api.features.planninggroups.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlanningGroupRequest {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String name;
}