package com.nutriplan.api.features.recipes.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.recipes.domain.Ingredient;
import com.nutriplan.api.features.recipes.domain.repository.IngredientRepository;
import com.nutriplan.api.features.recipes.dto.CreateIngredientRequest;
import com.nutriplan.api.features.recipes.dto.IngredientResponse;
import com.nutriplan.api.features.recipes.mapper.IngredientMapper;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientResponse createIngredient(CreateIngredientRequest request) {
        String normalizedName = normalizeRequiredText(request.getName());

        validateIngredientDoesNotExist(normalizedName);

        Ingredient ingredient = Ingredient.builder()
                .name(normalizedName)
                .defaultUnit(request.getDefaultUnit())
                .category(request.getCategory())
                .build();

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(savedIngredient);
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllIngredients() {
        return ingredientMapper.toResponseList(
                ingredientRepository.findAllByOrderByNameAsc());
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(UUID ingredientId) {
        Ingredient ingredient = findIngredientByIdOrThrow(ingredientId);
        return ingredientMapper.toResponse(ingredient);
    }

    private void validateIngredientDoesNotExist(String ingredientName) {
        if (ingredientRepository.existsByNameIgnoreCase(ingredientName)) {
            throw new ConflictException("Ingredient already exists");
        }
    }

    private Ingredient findIngredientByIdOrThrow(UUID ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
    }

    private String normalizeRequiredText(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}