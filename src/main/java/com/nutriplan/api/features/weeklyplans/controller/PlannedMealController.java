package com.nutriplan.api.features.weeklyplans.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.nutriplan.api.features.weeklyplans.dto.CreatePlannedMealRequest;
import com.nutriplan.api.features.weeklyplans.dto.PlannedMealResponse;
import com.nutriplan.api.features.weeklyplans.services.PlannedMealService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-plans/{planId}/meals")
@RequiredArgsConstructor
@Validated
public class PlannedMealController {

    private final PlannedMealService plannedMealService;

    @PostMapping
    public ResponseEntity<PlannedMealResponse> addMeal(
            @PathVariable UUID planId,
            @Valid @RequestBody CreatePlannedMealRequest request) {

        PlannedMealResponse response = plannedMealService.addMeal(planId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlannedMealResponse>> getMealsByPlanId(@PathVariable UUID planId) {
        return ResponseEntity.ok(plannedMealService.getMealsByPlanId(planId));
    }
}