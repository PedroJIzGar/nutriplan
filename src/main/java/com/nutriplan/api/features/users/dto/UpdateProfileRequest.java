package com.nutriplan.api.features.users.dto;

import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class UpdateProfileRequest {
    @NotBlank(message = "First name is required")
        String firstName;

        @NotBlank(message = "Last name is required")
        String lastName;

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be greater than 0")
        Integer age;

        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be greater than 0")
        Double weight;

        @NotNull(message = "Height is required")
        @Positive(message = "Height must be greater than 0")
        Double height;

        @NotNull(message = "Activity level is required")
        ActivityLevel activityLevel;

        @NotNull(message = "Goal is required")
        Goal goal;
}
