package com.nutriplan.api.features.recipes.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nutriplan.api.features.recipes.dto.CreateIngredientRequest;
import com.nutriplan.api.features.recipes.dto.IngredientResponse;
import com.nutriplan.api.features.recipes.services.IngredientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request) {
        IngredientResponse response = ingredientService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @GetMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponse> getIngredientById(@PathVariable UUID ingredientId) {
        return ResponseEntity.ok(ingredientService.getIngredientById(ingredientId));
    }
}