package com.nutriplan.api.feature.planninggroups.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.nutriplan.api.features.planninggroups.dto.AddPlanningGroupMemberRequest;
import com.nutriplan.api.features.planninggroups.dto.CreatePlanningGroupRequest;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupMemberResponse;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupResponse;
import com.nutriplan.api.features.planninggroups.services.PlanningGroupService;
import com.nutriplan.api.features.planninggroups.controller.PlanningGroupController;
import com.nutriplan.api.features.planninggroups.domain.enums.PlanningGroupRole;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

@WebMvcTest(PlanningGroupController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        JacksonConfig.class
})
class PlanningGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PlanningGroupService planningGroupService;


    @Test
    @DisplayName("POST /api/v1/planning-groups without token should return 401")
    void createGroupWithoutTokenShouldReturnUnauthorized() throws Exception {
        CreatePlanningGroupRequest request = CreatePlanningGroupRequest.builder()
                .name("Casa Hugo")
                .build();

        mockMvc.perform(post("/api/v1/planning-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/planning-groups with valid JWT should return 201")
    void createGroupWithJwtShouldReturnCreated() throws Exception {
        CreatePlanningGroupRequest request = CreatePlanningGroupRequest.builder()
                .name("Casa Hugo")
                .build();

        when(planningGroupService.createGroup(any(CreatePlanningGroupRequest.class)))
                .thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/planning-groups")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/planning-groups/my with valid JWT should return 200")
    void getMyGroupsShouldReturnOk() throws Exception {
        when(planningGroupService.getMyGroups())
                .thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/planning-groups/my")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/planning-groups/{id} with valid JWT should return 200")
    void getGroupByIdShouldReturnOk() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(planningGroupService.getGroupById(groupId))
                .thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/planning-groups/{groupId}", groupId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/planning-groups/{id}/members with valid JWT should return 200")
    void addMemberShouldReturnOk() throws Exception {
        UUID groupId = UUID.randomUUID();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(UUID.randomUUID())
                .portionFactor(0.8)
                .build();

        when(planningGroupService.addMember(any(UUID.class), any(AddPlanningGroupMemberRequest.class)))
                .thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/planning-groups/{groupId}/members", groupId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/planning-groups with blank name should return 400")
    void createGroupWithBlankNameShouldReturnBadRequest() throws Exception {
        CreatePlanningGroupRequest request = CreatePlanningGroupRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/planning-groups")
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/planning-groups/{id}/members duplicate member should return 409")
    void addDuplicateMemberShouldReturnConflict() throws Exception {
        UUID groupId = UUID.randomUUID();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(UUID.randomUUID())
                .portionFactor(1.0)
                .build();

        when(planningGroupService.addMember(any(UUID.class), any(AddPlanningGroupMemberRequest.class)))
                .thenThrow(new ConflictException("El usuario ya pertenece a este grupo"));

        mockMvc.perform(post("/api/v1/planning-groups/{groupId}/members", groupId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/planning-groups/{id} not found should return 404")
    void getGroupByIdNotFoundShouldReturnNotFound() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(planningGroupService.getGroupById(groupId))
                .thenThrow(new ResourceNotFoundException("Grupo no encontrado"));

        mockMvc.perform(get("/api/v1/planning-groups/{groupId}", groupId)
                .with(jwt().jwt(jwt -> jwt
                        .subject("0b342ef5-eec7-4e9a-ad21-45815544b9a8")
                        .claim("email", "admin@nutriplan.com"))))
                .andExpect(status().isNotFound());
    }

    private PlanningGroupResponse buildResponse() {
        return PlanningGroupResponse.builder()
                .id(UUID.randomUUID())
                .name("Casa Hugo")
                .createdByUserId(UUID.fromString("0b342ef5-eec7-4e9a-ad21-45815544b9a8"))
                .createdAt(OffsetDateTime.now())
                .members(List.of(
                        PlanningGroupMemberResponse.builder()
                                .userId(UUID.fromString("0b342ef5-eec7-4e9a-ad21-45815544b9a8"))
                                .firstName("Hugo")
                                .lastName("Rios")
                                .email("admin@nutriplan.com")
                                .role(PlanningGroupRole.OWNER)
                                .portionFactor(1.0)
                                .build()))
                .build();
    }
}