package com.nutriplan.api.features.weeklyplans.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.nutriplan.api.features.weeklyplans.dto.CreateWeeklyPlanRequest;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanDetailResponse;
import com.nutriplan.api.features.weeklyplans.dto.WeeklyPlanResponse;
import com.nutriplan.api.features.weeklyplans.services.WeeklyPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-plans")
@RequiredArgsConstructor
@Validated
public class WeeklyPlanController {

    private final WeeklyPlanService weeklyPlanService;

    @PostMapping
    public ResponseEntity<WeeklyPlanResponse> create(@Valid @RequestBody CreateWeeklyPlanRequest request) {
        WeeklyPlanResponse response = weeklyPlanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WeeklyPlanResponse>> getMyPlans() {
        return ResponseEntity.ok(weeklyPlanService.getMyPlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<WeeklyPlanDetailResponse> getById(@PathVariable UUID planId) {
        return ResponseEntity.ok(weeklyPlanService.getById(planId));
    }
}