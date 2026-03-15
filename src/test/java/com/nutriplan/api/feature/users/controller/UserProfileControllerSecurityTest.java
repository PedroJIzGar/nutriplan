package com.nutriplan.api.feature.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriplan.api.core.config.SecurityConfig;
import com.nutriplan.api.core.exception.GlobalExceptionHandler;
import com.nutriplan.api.features.users.controller.UserProfileController;
import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;
import com.nutriplan.api.features.users.dto.CreateProfileRequest;
import com.nutriplan.api.features.users.dto.UpdateProfileRequest;
import com.nutriplan.api.features.users.dto.WeightRequest;
import com.nutriplan.api.features.users.services.UserProfileService;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(UserProfileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserProfileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserProfileService profileService;

    @Test
    @DisplayName("POST /api/v1/profiles without token should return 401")
    void createProfileWithoutTokenShouldReturnUnauthorized() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Hugo",
                "Rios",
                30,
                "MALE",
                180.0,
                89.0,
                ActivityLevel.MODERATELY_ACTIVE,
                Goal.LOSE_WEIGHT
        );

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/profiles/me without token should return 401")
    void getMyProfileWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/profiles/me without token should return 401")
    void updateProfileWithoutTokenShouldReturnUnauthorized() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Hugo",
                "Rios",
                31,
                88.0,
                180.0,
                ActivityLevel.MODERATELY_ACTIVE,
                Goal.MAINTAIN_WEIGHT
        );

        mockMvc.perform(put("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/profiles/me without token should return 401")
    void deleteProfileWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/profiles/me/weights without token should return 401")
    void addWeightWithoutTokenShouldReturnUnauthorized() throws Exception {
        WeightRequest request = new WeightRequest(87.5);

        mockMvc.perform(post("/api/v1/profiles/me/weights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/profiles/me with valid JWT should return 200")
    void getMyProfileWithJwtShouldReturnOk() throws Exception {
        UserProfile profile = buildProfile();

        when(profileService.getMyProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/v1/profiles/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                                .claim("email", "admin@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/profiles with valid JWT should return 201")
    void createProfileWithJwtShouldReturnCreated() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Hugo",
                "Rios",
                30,
                "MALE",
                180.0,
                89.0,
                ActivityLevel.MODERATELY_ACTIVE,
                Goal.LOSE_WEIGHT
        );

        when(profileService.createProfile(any(CreateProfileRequest.class))).thenReturn(buildProfile());

        mockMvc.perform(post("/api/v1/profiles")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                                .claim("email", "admin@nutriplan.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/profiles with duplicate profile should return 409")
    void createProfileWhenAlreadyExistsShouldReturnConflict() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Hugo",
                "Rios",
                30,
                "MALE",
                180.0,
                89.0,
                ActivityLevel.MODERATELY_ACTIVE,
                Goal.LOSE_WEIGHT
        );

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenThrow(new ConflictException("El perfil del usuario ya existe"));

        mockMvc.perform(post("/api/v1/profiles")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                                .claim("email", "admin@nutriplan.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/profiles/me when profile does not exist should return 404")
    void getMyProfileWhenNotFoundShouldReturnNotFound() throws Exception {
        when(profileService.getMyProfile())
                .thenThrow(new ResourceNotFoundException("Perfil no encontrado"));

        mockMvc.perform(get("/api/v1/profiles/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                                .claim("email", "admin@nutriplan.com"))))
                .andExpect(status().isNotFound());
    }

    private UserProfile buildProfile() {
        return UserProfile.builder()
                .userId(UUID.fromString("0b342ef5-eec7-4e9a-ad21-45815544b9a8"))
                .firstName("Hugo")
                .lastName("Rios")
                .email("admin@nutriplan.com")
                .age(30)
                .gender("MALE")
                .height(180.0)
                .weight(89.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(Goal.LOSE_WEIGHT)
                .targetKcal(2200)
                .targetProtein(178)
                .targetFat(89)
                .targetCarbs(180)
                .isConfigured(true)
                .build();
    }
}