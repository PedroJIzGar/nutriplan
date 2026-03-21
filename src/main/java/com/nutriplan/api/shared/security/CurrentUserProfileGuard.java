package com.nutriplan.api.shared.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nutriplan.api.features.users.domain.repository.UserProfileRepository;
import com.nutriplan.api.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserProfileGuard {

    private final UserProfileRepository userProfileRepository;

    public void ensureExists(UUID userId) {
        boolean exists = userProfileRepository.existsById(userId);

        if (!exists) {
            throw new BadRequestException("User profile must exist before creating a weekly plan");
        }
    }
}