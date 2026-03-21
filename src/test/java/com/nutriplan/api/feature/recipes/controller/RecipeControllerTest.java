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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriplan.api.core.config.SecurityConfig;
import com.nutriplan.api.core.exception.GlobalExceptionHandler;
import com.nutriplan.api.core.security.CustomAccessDeniedHandler;
import com.nutriplan.api.core.security.CustomAuthenticationEntryPoint;
import com.nutriplan.api.features.recipes.controller.RecipeController;
import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;
import com.nutriplan.api.features.recipes.domain.enums.MealType;
import com.nutriplan.api.features.recipes.dto.CreateRecipeIngredientRequest;
import com.nutriplan.api.features.recipes.dto.CreateRecipeRequest;
import com.nutriplan.api.features.recipes.dto.RecipeIngredientResponse;
import com.nutriplan.api.features.recipes.dto.RecipeResponse;
import com.nutriplan.api.features.recipes.dto.RecipeSummaryResponse;
import com.nutriplan.api.features.recipes.services.RecipeService;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(controllers = RecipeController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class RecipeControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        @MockitoBean
        private RecipeService recipeService;

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
        @DisplayName("POST /api/v1/recipes should return 201 when request is valid")
        void createRecipeShouldReturnCreatedWhenRequestIsValid() throws Exception {
                UUID chickenId = UUID.randomUUID();
                UUID recipeId = UUID.randomUUID();
                UUID recipeIngredientId = UUID.randomUUID();

                CreateRecipeRequest request = CreateRecipeRequest.builder()
                                .name("Chicken Bowl")
                                .description("High protein lunch")
                                .mealType(MealType.LUNCH)
                                .servings(2)
                                .targetKcal(new BigDecimal("650.00"))
                                .protein(new BigDecimal("45.00"))
                                .carbs(new BigDecimal("60.00"))
                                .fat(new BigDecimal("15.00"))
                                .prepTimeMinutes(20)
                                .ingredients(List.of(
                                                CreateRecipeIngredientRequest.builder()
                                                                .ingredientId(chickenId)
                                                                .quantity(new BigDecimal("200.00"))
                                                                .unit(IngredientUnit.GRAM)
                                                                .notes("grilled")
                                                                .build()))
                                .build();

                RecipeResponse response = RecipeResponse.builder()
                                .id(recipeId)
                                .name("Chicken Bowl")
                                .description("High protein lunch")
                                .mealType(MealType.LUNCH)
                                .servings(2)
                                .targetKcal(new BigDecimal("650.00"))
                                .protein(new BigDecimal("45.00"))
                                .carbs(new BigDecimal("60.00"))
                                .fat(new BigDecimal("15.00"))
                                .prepTimeMinutes(20)
                                .active(true)
                                .createdAt(OffsetDateTime.now())
                                .ingredients(List.of(
                                                RecipeIngredientResponse.builder()
                                                                .id(recipeIngredientId)
                                                                .ingredientId(chickenId)
                                                                .ingredientName("Chicken")
                                                                .quantity(new BigDecimal("200.00"))
                                                                .unit(IngredientUnit.GRAM)
                                                                .notes("grilled")
                                                                .build()))
                                .build();

                when(recipeService.createRecipe(any(CreateRecipeRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/recipes")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(recipeId.toString()))
                                .andExpect(jsonPath("$.name").value("Chicken Bowl"))
                                .andExpect(jsonPath("$.description").value("High protein lunch"))
                                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                                .andExpect(jsonPath("$.servings").value(2))
                                .andExpect(jsonPath("$.targetKcal").value(650.00))
                                .andExpect(jsonPath("$.protein").value(45.00))
                                .andExpect(jsonPath("$.carbs").value(60.00))
                                .andExpect(jsonPath("$.fat").value(15.00))
                                .andExpect(jsonPath("$.prepTimeMinutes").value(20))
                                .andExpect(jsonPath("$.active").value(true))
                                .andExpect(jsonPath("$.ingredients[0].id").value(recipeIngredientId.toString()))
                                .andExpect(jsonPath("$.ingredients[0].ingredientId").value(chickenId.toString()))
                                .andExpect(jsonPath("$.ingredients[0].ingredientName").value("Chicken"))
                                .andExpect(jsonPath("$.ingredients[0].quantity").value(200.00))
                                .andExpect(jsonPath("$.ingredients[0].unit").value("GRAM"))
                                .andExpect(jsonPath("$.ingredients[0].notes").value("grilled"));

                verify(recipeService).createRecipe(any(CreateRecipeRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/recipes should return 400 when request is invalid")
        void createRecipeShouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
                CreateRecipeRequest request = CreateRecipeRequest.builder()
                                .name(" ")
                                .description(" ")
                                .mealType(null)
                                .servings(0)
                                .targetKcal(new BigDecimal("0.00"))
                                .protein(null)
                                .carbs(null)
                                .fat(null)
                                .prepTimeMinutes(0)
                                .ingredients(List.of())
                                .build();

                mockMvc.perform(post("/api/v1/recipes")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/recipes should return 409 when recipe contains duplicated ingredients")
        void createRecipeShouldReturnConflictWhenRecipeContainsDuplicatedIngredients() throws Exception {
                UUID ingredientId = UUID.randomUUID();

                CreateRecipeRequest request = CreateRecipeRequest.builder()
                                .name("Omelette")
                                .description("Quick dinner")
                                .mealType(MealType.DINNER)
                                .servings(1)
                                .targetKcal(new BigDecimal("300.00"))
                                .protein(new BigDecimal("20.00"))
                                .carbs(new BigDecimal("5.00"))
                                .fat(new BigDecimal("18.00"))
                                .prepTimeMinutes(10)
                                .ingredients(List.of(
                                                CreateRecipeIngredientRequest.builder()
                                                                .ingredientId(ingredientId)
                                                                .quantity(new BigDecimal("2.00"))
                                                                .unit(IngredientUnit.UNIT)
                                                                .build()))
                                .build();

                when(recipeService.createRecipe(any(CreateRecipeRequest.class)))
                                .thenThrow(new ConflictException("Recipe contains duplicated ingredients"));

                mockMvc.perform(post("/api/v1/recipes")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());

                verify(recipeService).createRecipe(any(CreateRecipeRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/recipes without token should return 401")
        void createRecipeWithoutTokenShouldReturnUnauthorized() throws Exception {
                UUID ingredientId = UUID.randomUUID();

                CreateRecipeRequest request = CreateRecipeRequest.builder()
                                .name("Chicken Bowl")
                                .description("High protein lunch")
                                .mealType(MealType.LUNCH)
                                .servings(2)
                                .targetKcal(new BigDecimal("650.00"))
                                .protein(new BigDecimal("45.00"))
                                .carbs(new BigDecimal("60.00"))
                                .fat(new BigDecimal("15.00"))
                                .prepTimeMinutes(20)
                                .ingredients(List.of(
                                                CreateRecipeIngredientRequest.builder()
                                                                .ingredientId(ingredientId)
                                                                .quantity(new BigDecimal("200.00"))
                                                                .unit(IngredientUnit.GRAM)
                                                                .notes("grilled")
                                                                .build()))
                                .build();

                mockMvc.perform(post("/api/v1/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/recipes should return 200 with recipe summary list")
        void getAllRecipesShouldReturnOkWithRecipeSummaryList() throws Exception {
                UUID recipeId = UUID.randomUUID();

                RecipeSummaryResponse response = RecipeSummaryResponse.builder()
                                .id(recipeId)
                                .name("Porridge")
                                .description("Breakfast recipe")
                                .mealType(MealType.BREAKFAST)
                                .servings(1)
                                .targetKcal(new BigDecimal("350.00"))
                                .protein(new BigDecimal("15.00"))
                                .carbs(new BigDecimal("45.00"))
                                .fat(new BigDecimal("8.00"))
                                .prepTimeMinutes(10)
                                .active(true)
                                .createdAt(OffsetDateTime.now())
                                .build();

                when(recipeService.getAllRecipes()).thenReturn(List.of(response));

                mockMvc.perform(get("/api/v1/recipes")
                                .with(jwt()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(recipeId.toString()))
                                .andExpect(jsonPath("$[0].name").value("Porridge"))
                                .andExpect(jsonPath("$[0].description").value("Breakfast recipe"))
                                .andExpect(jsonPath("$[0].mealType").value("BREAKFAST"))
                                .andExpect(jsonPath("$[0].servings").value(1))
                                .andExpect(jsonPath("$[0].targetKcal").value(350.00))
                                .andExpect(jsonPath("$[0].protein").value(15.00))
                                .andExpect(jsonPath("$[0].carbs").value(45.00))
                                .andExpect(jsonPath("$[0].fat").value(8.00))
                                .andExpect(jsonPath("$[0].prepTimeMinutes").value(10))
                                .andExpect(jsonPath("$[0].active").value(true));

                verify(recipeService).getAllRecipes();
        }

        @Test
        @DisplayName("GET /api/v1/recipes/{id} should return 200 when recipe exists")
        void getRecipeByIdShouldReturnOkWhenRecipeExists() throws Exception {
                UUID recipeId = UUID.randomUUID();

                RecipeResponse response = RecipeResponse.builder()
                                .id(recipeId)
                                .name("Porridge")
                                .description("Breakfast recipe")
                                .mealType(MealType.BREAKFAST)
                                .servings(1)
                                .targetKcal(new BigDecimal("350.00"))
                                .protein(new BigDecimal("15.00"))
                                .carbs(new BigDecimal("45.00"))
                                .fat(new BigDecimal("8.00"))
                                .prepTimeMinutes(10)
                                .active(true)
                                .createdAt(OffsetDateTime.now())
                                .ingredients(List.of())
                                .build();

                when(recipeService.getRecipeById(recipeId)).thenReturn(response);

                mockMvc.perform(get("/api/v1/recipes/{recipeId}", recipeId)
                                .with(jwt()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(recipeId.toString()))
                                .andExpect(jsonPath("$.name").value("Porridge"))
                                .andExpect(jsonPath("$.description").value("Breakfast recipe"))
                                .andExpect(jsonPath("$.mealType").value("BREAKFAST"))
                                .andExpect(jsonPath("$.servings").value(1))
                                .andExpect(jsonPath("$.targetKcal").value(350.00))
                                .andExpect(jsonPath("$.protein").value(15.00))
                                .andExpect(jsonPath("$.carbs").value(45.00))
                                .andExpect(jsonPath("$.fat").value(8.00))
                                .andExpect(jsonPath("$.prepTimeMinutes").value(10))
                                .andExpect(jsonPath("$.active").value(true));

                verify(recipeService).getRecipeById(recipeId);
        }

        @Test
        @DisplayName("GET /api/v1/recipes/{id} should return 404 when recipe does not exist")
        void getRecipeByIdShouldReturnNotFoundWhenRecipeDoesNotExist() throws Exception {
                UUID recipeId = UUID.randomUUID();

                when(recipeService.getRecipeById(eq(recipeId)))
                                .thenThrow(new ResourceNotFoundException("Recipe not found"));

                mockMvc.perform(get("/api/v1/recipes/{recipeId}", recipeId)
                                .with(jwt()))
                                .andExpect(status().isNotFound());

                verify(recipeService).getRecipeById(recipeId);
        }

        @Test
        @DisplayName("GET /api/v1/recipes without token should return 401")
        void getAllRecipesWithoutTokenShouldReturnUnauthorized() throws Exception {
                mockMvc.perform(get("/api/v1/recipes"))
                                .andExpect(status().isUnauthorized());
        }
}