package com.nutriplan.api.features.planninggroups.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nutriplan.api.features.planninggroups.dto.AddPlanningGroupMemberRequest;
import com.nutriplan.api.features.planninggroups.dto.CreatePlanningGroupRequest;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupResponse;
import com.nutriplan.api.features.planninggroups.services.PlanningGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/planning-groups")
@RequiredArgsConstructor
public class PlanningGroupController {

    private final PlanningGroupService planningGroupService;

    @PostMapping
    public ResponseEntity<PlanningGroupResponse> createGroup(
            @Valid @RequestBody CreatePlanningGroupRequest request) {
        PlanningGroupResponse response = planningGroupService.createGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<PlanningGroupResponse>> getMyGroups() {
        return ResponseEntity.ok(planningGroupService.getMyGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<PlanningGroupResponse> getGroupById(@PathVariable UUID groupId) {
        return ResponseEntity.ok(planningGroupService.getGroupById(groupId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<PlanningGroupResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddPlanningGroupMemberRequest request) {
        return ResponseEntity.ok(planningGroupService.addMember(groupId, request));
    }
}