package com.nutriplan.api.features.planninggroups.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutriplan.api.features.planninggroups.domain.PlanningGroupMember;

public interface PlanningGroupMemberRepository extends JpaRepository<PlanningGroupMember, UUID> {

    List<PlanningGroupMember> findByUserProfileUserId(UUID userId);

    List<PlanningGroupMember> findByPlanningGroupId(UUID planningGroupId);

    boolean existsByPlanningGroupIdAndUserProfileUserId(UUID planningGroupId, UUID userId);

    Optional<PlanningGroupMember> findByPlanningGroupIdAndUserProfileUserId(UUID planningGroupId, UUID userId);
}