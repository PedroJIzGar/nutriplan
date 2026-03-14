package com.nutriplan.api.features.users.domain.repository;

import com.nutriplan.api.features.users.domain.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, UUID> {
    List<WeightLog> findByUserUserIdOrderByLogDateDesc(UUID userId);
}