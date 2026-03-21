package com.nutriplan.api.feature.recipes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriplan.api.core.config.SecurityConfig;
import com.nutriplan.api.core.exception.GlobalExceptionHandler;
import com.nutriplan.api.core.security.CustomAccessDeniedHandler;
import com.nutriplan.api.core.security.CustomAuthenticationEntryPoint;
import com.nutriplan.api.features.recipes.controller.IngredientController;
import com.nutriplan.api.features.recipes.domain.enums.IngredientCategory;
import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;
import com.nutriplan.api.features.recipes.dto.CreateIngredientRequest;
import com.nutriplan.api.features.recipes.dto.IngredientResponse;
import com.nutriplan.api.features.recipes.services.IngredientService;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = IngredientController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IngredientService ingredientService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpSecurityHandlers() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        }).when(customAuthenticationEntryPoint)
                .commence(any(), any(), any(AuthenticationException.class));

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return null;
        }).when(customAccessDeniedHandler)
                .handle(any(), any(), any(AccessDeniedException.class));
    }

    @Test
    @DisplayName("POST /api/v1/ingredients should return 201 when request is valid")
    void createIngredientShouldReturnCreatedWhenRequestIsValid() throws Exception {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        IngredientResponse response = IngredientResponse.builder()
                .id(UUID.randomUUID())
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .createdAt(OffsetDateTime.now())
                .build();

        when(ingredientService.createIngredient(any(CreateIngredientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.name").value("Rice"))
                .andExpect(jsonPath("$.defaultUnit").value("GRAM"))
                .andExpect(jsonPath("$.category").value("CEREAL"));

        verify(ingredientService).createIngredient(any(CreateIngredientRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/ingredients should return 400 when request is invalid")
    void createIngredientShouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name(" ")
                .defaultUnit(null)
                .category(null)
                .build();

        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("POST /api/v1/ingredients should return 409 when ingredient already exists")
    void createIngredientShouldReturnConflictWhenIngredientAlreadyExists() throws Exception {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        when(ingredientService.createIngredient(any(CreateIngredientRequest.class)))
                .thenThrow(new ConflictException("Ingredient already exists"));

        mockMvc.perform(post("/api/v1/ingredients")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(ingredientService).createIngredient(any(CreateIngredientRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/ingredients without token should return 401")
    void createIngredientWithoutTokenShouldReturnUnauthorized() throws Exception {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        mockMvc.perform(post("/api/v1/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/ingredients should return 200 with ingredient list")
    void getAllIngredientsShouldReturnOkWithIngredientList() throws Exception {
        IngredientResponse ingredient = IngredientResponse.builder()
                .id(UUID.randomUUID())
                .name("Chicken")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.MEAT)
                .createdAt(OffsetDateTime.now())
                .build();

        when(ingredientService.getAllIngredients()).thenReturn(List.of(ingredient));

        mockMvc.perform(get("/api/v1/ingredients")
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ingredient.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Chicken"))
                .andExpect(jsonPath("$[0].defaultUnit").value("GRAM"))
                .andExpect(jsonPath("$[0].category").value("MEAT"));

        verify(ingredientService).getAllIngredients();
    }

    @Test
    @DisplayName("GET /api/v1/ingredients/{id} should return 200 when ingredient exists")
    void getIngredientByIdShouldReturnOkWhenIngredientExists() throws Exception {
        UUID ingredientId = UUID.randomUUID();

        IngredientResponse response = IngredientResponse.builder()
                .id(ingredientId)
                .name("Salmon")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.FISH)
                .createdAt(OffsetDateTime.now())
                .build();

        when(ingredientService.getIngredientById(ingredientId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", ingredientId)
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ingredientId.toString()))
                .andExpect(jsonPath("$.name").value("Salmon"))
                .andExpect(jsonPath("$.defaultUnit").value("GRAM"))
                .andExpect(jsonPath("$.category").value("FISH"));

        verify(ingredientService).getIngredientById(ingredientId);
    }

    @Test
    @DisplayName("GET /api/v1/ingredients/{id} should return 404 when ingredient does not exist")
    void getIngredientByIdShouldReturnNotFoundWhenIngredientDoesNotExist() throws Exception {
        UUID ingredientId = UUID.randomUUID();

        when(ingredientService.getIngredientById(eq(ingredientId)))
                .thenThrow(new ResourceNotFoundException("Ingredient not found"));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", ingredientId)
                .with(jwt()))
                .andExpect(status().isNotFound());

        verify(ingredientService).getIngredientById(ingredientId);
    }

    @Test
    @DisplayName("GET /api/v1/ingredients without token should return 401")
    void getAllIngredientsWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/ingredients"))
                .andExpect(status().isUnauthorized());
    }
}