package com.nutriplan.api.features.users.dto;

import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfileRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank
    @Email(message = "Formato de email inválido")
    private String email;

    @NotNull
    @Min(1)
    private Integer age;

    @NotBlank
    private String gender;

    @NotNull
    @Min(1)
    private Double height;

    @NotNull
    @Min(1)
    private Double weight;

    @NotNull(message = "El nivel de actividad es obligatorio")
    private ActivityLevel activityLevel; // Usar el Enum directamente

    @NotNull(message = "El objetivo es obligatorio")
    private Goal goal; // Usar el Enum directamente
}