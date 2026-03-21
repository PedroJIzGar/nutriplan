package com.nutriplan.api.feature.weeklyplans.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriplan.api.core.config.JacksonConfig;
import com.nutriplan.api.core.config.SecurityConfig;
import com.nutriplan.api.core.exception.GlobalExceptionHandler;
import com.nutriplan.api.core.security.CustomAccessDeniedHandler;
import com.nutriplan.api.core.security.CustomAuthenticationEntryPoint;
import com.nutriplan.api.features.weeklyplans.controller.WeeklyPlanController;
import com.nutriplan.api.features.weeklyplans.dto.CreateWeeklyPlanRequest;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanDetailResponse;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanResponse;
import com.nutriplan.api.features.weeklyplans.services.WeeklyPlanService;
import com.nutriplan.api.shared.exception.BadRequestException;
import com.nutriplan.api.shared.exception.ConflictException;

import com.nutriplan.api.shared.exception.ResourceNotFoundException;

@WebMvcTest(WeeklyPlanController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        JacksonConfig.class
})
class WeeklyPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private WeeklyPlanService weeklyPlanService;

    @Test
    @DisplayName("POST /api/v1/weekly-plans without token should return 401")
    void createWithoutTokenShouldReturnUnauthorized() throws Exception {
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .build();

        mockMvc.perform(post("/api/v1/weekly-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans with valid JWT should return 201")
    void createWithJwtShouldReturnCreated() throws Exception {
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .build();

        when(weeklyPlanService.create(any(CreateWeeklyPlanRequest.class)))
                .thenReturn(buildWeeklyPlanResponse());

        mockMvc.perform(post("/api/v1/weekly-plans")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans with missing startDate should return 400")
    void createWithMissingStartDateShouldReturnBadRequest() throws Exception {
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .build();

        mockMvc.perform(post("/api/v1/weekly-plans")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans with non-monday startDate should return 400")
    void createWithNonMondayStartDateShouldReturnBadRequest() throws Exception {
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 24))
                .build();

        when(weeklyPlanService.create(any(CreateWeeklyPlanRequest.class)))
                .thenThrow(new BadRequestException("Start date must be a Monday"));

        mockMvc.perform(post("/api/v1/weekly-plans")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans duplicate week should return 409")
    void createDuplicateWeekShouldReturnConflict() throws Exception {
        CreateWeeklyPlanRequest request = CreateWeeklyPlanRequest.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .build();

        when(weeklyPlanService.create(any(CreateWeeklyPlanRequest.class)))
                .thenThrow(new ConflictException("A weekly plan already exists for this start date"));

        mockMvc.perform(post("/api/v1/weekly-plans")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/weekly-plans with valid JWT should return 200")
    void getMyPlansShouldReturnOk() throws Exception {
        when(weeklyPlanService.getMyPlans())
                .thenReturn(List.of(buildWeeklyPlanResponse()));

        mockMvc.perform(get("/api/v1/weekly-plans")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/weekly-plans/{id} with valid JWT should return 200")
    void getByIdShouldReturnOk() throws Exception {
        UUID planId = UUID.randomUUID();

        when(weeklyPlanService.getById(planId))
                .thenReturn(buildWeeklyPlanDetailResponse(planId));

        mockMvc.perform(get("/api/v1/weekly-plans/{planId}", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/weekly-plans/{id} not found should return 404")
    void getByIdNotFoundShouldReturnNotFound() throws Exception {
        UUID planId = UUID.randomUUID();

        when(weeklyPlanService.getById(planId))
                .thenThrow(new ResourceNotFoundException("Weekly plan not found"));

        mockMvc.perform(get("/api/v1/weekly-plans/{planId}", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com"))))
                .andExpect(status().isNotFound());
    }

    private WeeklyPlanResponse buildWeeklyPlanResponse() {
        return WeeklyPlanResponse.builder()
                .id(UUID.fromString("74dc2b64-830a-421c-acd0-1bd0eda812f2"))
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 29))
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:00Z"))
                .build();
    }

    private WeeklyPlanDetailResponse buildWeeklyPlanDetailResponse(UUID planId) {
        return WeeklyPlanDetailResponse.builder()
                .id(planId)
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 29))
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:00Z"))
                .plannedMeals(List.of())
                .build();
    }
}