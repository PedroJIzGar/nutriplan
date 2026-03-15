package com.nutriplan.api.features.planninggroups.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nutriplan.api.features.planninggroups.domain.PlanningGroup;

public interface PlanningGroupRepository extends JpaRepository<PlanningGroup, UUID> {

    @Query("""
                select distinct pg
                from PlanningGroup pg
                left join fetch pg.members m
                left join fetch m.userProfile
                where pg.id = :groupId
            """)
    Optional<PlanningGroup> findByIdWithMembers(UUID groupId);

    @Query("""
                select distinct pg
                from PlanningGroup pg
                join pg.members memberFilter
                left join fetch pg.members m
                left join fetch m.userProfile
                where memberFilter.userProfile.userId = :userId
            """)
    List<PlanningGroup> findAllByUserIdWithMembers(UUID userId);

    List<PlanningGroup> findByCreatedByUserId(UUID createdByUserId);
}