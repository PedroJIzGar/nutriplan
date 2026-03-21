package com.nutriplan.api.features.weeklyplans.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nutriplan.api.features.weeklyplans.domain.WeeklyPlan;

public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, UUID> {

    Optional<WeeklyPlan> findByIdAndUserId(UUID id, UUID userId);

    List<WeeklyPlan> findAllByUserIdOrderByStartDateDesc(UUID userId);

    boolean existsByUserIdAndStartDate(UUID userId, LocalDate startDate);

    @Query("""
                SELECT DISTINCT wp
                FROM WeeklyPlan wp
                LEFT JOIN FETCH wp.plannedMeals pm
                LEFT JOIN FETCH pm.recipe r
                WHERE wp.id = :planId
                  AND wp.userId = :userId
            """)
    Optional<WeeklyPlan> findDetailedByIdAndUserId(UUID planId, UUID userId);
}