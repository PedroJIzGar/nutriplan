package com.nutriplan.api.feature.users.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriplan.api.core.config.SecurityConfig;
import com.nutriplan.api.core.exception.GlobalExceptionHandler;
import com.nutriplan.api.features.users.controller.UserProfileController;
import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;
import com.nutriplan.api.features.users.dto.CreateProfileRequest;
import com.nutriplan.api.features.users.dto.UpdateProfileRequest;
import com.nutriplan.api.features.users.dto.WeightRequest;
import com.nutriplan.api.features.users.services.UserProfileService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserProfileController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class UserProfileControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserProfileService profileService;

    @Test
    @DisplayName("POST /api/v1/profiles with blank first name should return 400")
    void createProfileWithBlankFirstNameShouldReturnBadRequest() throws Exception {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("")
                .lastName("Rios")
                .age(30)
                .gender("MALE")
                .height(180.0)
                .weight(89.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(Goal.LOSE_WEIGHT)
                .build();

        mockMvc.perform(post("/api/v1/profiles")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.firstName").exists());
    }

    @Test
    @DisplayName("POST /api/v1/profiles with null goal should return 400")
    void createProfileWithNullGoalShouldReturnBadRequest() throws Exception {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("Hugo")
                .lastName("Rios")
                .age(30)
                .gender("MALE")
                .height(180.0)
                .weight(89.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(null)
                .build();

        mockMvc.perform(post("/api/v1/profiles")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.goal").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/profiles/me with negative weight should return 400")
    void updateProfileWithNegativeWeightShouldReturnBadRequest() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Hugo")
                .lastName("Rios")
                .age(30)
                .weight(-5.0)
                .height(180.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(Goal.MAINTAIN_WEIGHT)
                .build();

        mockMvc.perform(put("/api/v1/profiles/me")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.weight").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/profiles/me with blank last name should return 400")
    void updateProfileWithBlankLastNameShouldReturnBadRequest() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Hugo")
                .lastName("")
                .age(30)
                .weight(88.0)
                .height(180.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(Goal.MAINTAIN_WEIGHT)
                .build();

        mockMvc.perform(put("/api/v1/profiles/me")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.lastName").exists());
    }

    @Test
    @DisplayName("POST /api/v1/profiles/me/weights with null weight should return 400")
    void addWeightWithNullWeightShouldReturnBadRequest() throws Exception {
        WeightRequest request = WeightRequest.builder()
                .weight(null)
                .build();

        mockMvc.perform(post("/api/v1/profiles/me/weights")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.weight").exists());
    }

    @Test
    @DisplayName("POST /api/v1/profiles/me/weights with zero weight should return 400")
    void addWeightWithZeroWeightShouldReturnBadRequest() throws Exception {
        WeightRequest request = WeightRequest.builder()
                .weight(0.0)
                .build();

        mockMvc.perform(post("/api/v1/profiles/me/weights")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.weight").exists());
    }
}