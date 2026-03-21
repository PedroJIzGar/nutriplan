package com.nutriplan.api.feature.recipes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nutriplan.api.features.recipes.domain.Ingredient;
import com.nutriplan.api.features.recipes.domain.enums.IngredientCategory;
import com.nutriplan.api.features.recipes.domain.enums.IngredientUnit;
import com.nutriplan.api.features.recipes.domain.repository.IngredientRepository;
import com.nutriplan.api.features.recipes.dto.CreateIngredientRequest;
import com.nutriplan.api.features.recipes.dto.IngredientResponse;
import com.nutriplan.api.features.recipes.mapper.IngredientMapper;
import com.nutriplan.api.features.recipes.services.IngredientService;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    @DisplayName("createIngredient should create ingredient when name does not exist")
    void createIngredientShouldCreateIngredientWhenNameDoesNotExist() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("  Brown   Rice  ")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        Ingredient savedIngredient = Ingredient.builder()
                .id(UUID.randomUUID())
                .name("Brown Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .createdAt(OffsetDateTime.now())
                .build();

        IngredientResponse response = IngredientResponse.builder()
                .id(savedIngredient.getId())
                .name(savedIngredient.getName())
                .defaultUnit(savedIngredient.getDefaultUnit())
                .category(savedIngredient.getCategory())
                .createdAt(savedIngredient.getCreatedAt())
                .build();

        when(ingredientRepository.existsByNameIgnoreCase("Brown Rice")).thenReturn(false);
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(savedIngredient);
        when(ingredientMapper.toResponse(savedIngredient)).thenReturn(response);

        IngredientResponse result = ingredientService.createIngredient(request);

        assertThat(result).isEqualTo(response);

        verify(ingredientRepository).existsByNameIgnoreCase("Brown Rice");
        verify(ingredientRepository).save(argThat(ingredient -> ingredient.getName().equals("Brown Rice")
                && ingredient.getDefaultUnit() == IngredientUnit.GRAM
                && ingredient.getCategory() == IngredientCategory.CEREAL));
        verify(ingredientMapper).toResponse(savedIngredient);
    }

    @Test
    @DisplayName("createIngredient should throw conflict when ingredient already exists")
    void createIngredientShouldThrowConflictWhenIngredientAlreadyExists() {
        CreateIngredientRequest request = CreateIngredientRequest.builder()
                .name("Rice")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.CEREAL)
                .build();

        when(ingredientRepository.existsByNameIgnoreCase("Rice")).thenReturn(true);

        assertThrows(ConflictException.class, () -> ingredientService.createIngredient(request));

        verify(ingredientRepository).existsByNameIgnoreCase("Rice");
        verify(ingredientRepository, never()).save(any());
        verify(ingredientMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("getAllIngredients should return mapped ingredient list")
    void getAllIngredientsShouldReturnMappedIngredientList() {
        Ingredient ingredient = Ingredient.builder()
                .id(UUID.randomUUID())
                .name("Chicken")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.MEAT)
                .build();

        IngredientResponse response = IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .defaultUnit(ingredient.getDefaultUnit())
                .category(ingredient.getCategory())
                .build();

        when(ingredientRepository.findAllByOrderByNameAsc()).thenReturn(List.of(ingredient));
        when(ingredientMapper.toResponseList(List.of(ingredient))).thenReturn(List.of(response));

        List<IngredientResponse> result = ingredientService.getAllIngredients();

        assertThat(result).containsExactly(response);

        verify(ingredientRepository).findAllByOrderByNameAsc();
        verify(ingredientMapper).toResponseList(List.of(ingredient));
    }

    @Test
    @DisplayName("getIngredientById should return ingredient when found")
    void getIngredientByIdShouldReturnIngredientWhenFound() {
        UUID ingredientId = UUID.randomUUID();

        Ingredient ingredient = Ingredient.builder()
                .id(ingredientId)
                .name("Salmon")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.FISH)
                .build();

        IngredientResponse response = IngredientResponse.builder()
                .id(ingredientId)
                .name("Salmon")
                .defaultUnit(IngredientUnit.GRAM)
                .category(IngredientCategory.FISH)
                .build();

        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));
        when(ingredientMapper.toResponse(ingredient)).thenReturn(response);

        IngredientResponse result = ingredientService.getIngredientById(ingredientId);

        assertThat(result).isEqualTo(response);

        verify(ingredientRepository).findById(ingredientId);
        verify(ingredientMapper).toResponse(ingredient);
    }

    @Test
    @DisplayName("getIngredientById should throw not found when ingredient does not exist")
    void getIngredientByIdShouldThrowNotFoundWhenIngredientDoesNotExist() {
        UUID ingredientId = UUID.randomUUID();

        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ingredientService.getIngredientById(ingredientId));

        verify(ingredientRepository).findById(ingredientId);
        verify(ingredientMapper, never()).toResponse(any());
    }
}