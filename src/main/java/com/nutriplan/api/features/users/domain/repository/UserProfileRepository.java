package com.nutriplan.api.features.users.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutriplan.api.features.users.domain.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, java.util.UUID> {
    
    Optional<UserProfile> findByUserId(java.util.UUID userId);
    boolean existsByEmail(String email);
}
