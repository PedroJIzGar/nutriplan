package com.nutriplan.api.feature.weeklyplans.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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
import com.nutriplan.api.features.recipes.domain.enums.MealType;
import com.nutriplan.api.features.weeklyplans.controller.PlannedMealController;
import com.nutriplan.api.features.weeklyplans.dto.CreatePlannedMealRequest;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;
import com.nutriplan.api.features.weeklyplans.services.PlannedMealService;
import com.nutriplan.api.shared.exception.BadRequestException;
import com.nutriplan.api.shared.exception.ConflictException;

import com.nutriplan.api.shared.exception.ResourceNotFoundException;

@WebMvcTest(PlannedMealController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        JacksonConfig.class
})
class PlannedMealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PlannedMealService plannedMealService;

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals without token should return 401")
    void addMealWithoutTokenShouldReturnUnauthorized() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = buildRequest();

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals with valid JWT should return 201")
    void addMealWithJwtShouldReturnCreated() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = buildRequest();

        when(plannedMealService.addMeal(eq(planId), any(CreatePlannedMealRequest.class)))
                .thenReturn(buildResponse(planId));

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals with missing fields should return 400")
    void addMealWithMissingFieldsShouldReturnBadRequest() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = CreatePlannedMealRequest.builder().build();

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals with missing recipe should return 404")
    void addMealWithMissingRecipeShouldReturnNotFound() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = buildRequest();

        when(plannedMealService.addMeal(eq(planId), any(CreatePlannedMealRequest.class)))
                .thenThrow(new ResourceNotFoundException("Recipe not found"));

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals duplicate slot should return 409")
    void addMealDuplicateSlotShouldReturnConflict() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = buildRequest();

        when(plannedMealService.addMeal(eq(planId), any(CreatePlannedMealRequest.class)))
                .thenThrow(new ConflictException("A planned meal already exists for this day and meal type"));

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/weekly-plans/{id}/meals with inactive recipe should return 400")
    void addMealWithInactiveRecipeShouldReturnBadRequest() throws Exception {
        UUID planId = UUID.randomUUID();
        CreatePlannedMealRequest request = buildRequest();

        when(plannedMealService.addMeal(eq(planId), any(CreatePlannedMealRequest.class)))
                .thenThrow(new BadRequestException("Recipe is inactive"));

        mockMvc.perform(post("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weekly-plans/{id}/meals with valid JWT should return 200")
    void getMealsByPlanIdShouldReturnOk() throws Exception {
        UUID planId = UUID.randomUUID();

        when(plannedMealService.getMealsByPlanId(planId))
                .thenReturn(List.of(buildResponse(planId)));

        mockMvc.perform(get("/api/v1/weekly-plans/{planId}/meals", planId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "hugo@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    private CreatePlannedMealRequest buildRequest() {
        return CreatePlannedMealRequest.builder()
                .recipeId(UUID.fromString("22000000-0000-0000-0000-000000000020"))
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .build();
    }

    private PlannedMealResponse buildResponse(UUID planId) {
        return PlannedMealResponse.builder()
                .id(UUID.fromString("42127157-cdcc-4e59-8605-a7c01b92d93d"))
                .weeklyPlanId(planId)
                .recipeId(UUID.fromString("22000000-0000-0000-0000-000000000020"))
                .recipeName("Banana and Yogurt Cup")
                .dayOfWeek(DayOfWeek.MONDAY)
                .mealType(MealType.BREAKFAST)
                .targetKcal(BigDecimal.valueOf(190.00))
                .protein(BigDecimal.valueOf(10.00))
                .carbs(BigDecimal.valueOf(22.00))
                .fat(BigDecimal.valueOf(6.00))
                .createdAt(OffsetDateTime.parse("2026-03-21T14:55:29Z"))
                .build();
    }
}