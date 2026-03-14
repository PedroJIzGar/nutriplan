package com.nutriplan.api.feature.users.services;

import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;
import com.nutriplan.api.features.users.domain.repository.UserProfileRepository;
import com.nutriplan.api.features.users.domain.repository.WeightLogRepository;
import com.nutriplan.api.features.users.dto.CreateProfileRequest;
import com.nutriplan.api.features.users.services.UserProfileService;
import com.nutriplan.api.shared.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userRepository;
    @Mock
    private WeightLogRepository weightLogRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void shouldCalculateNutritionCorrectlyForMale() {
        // GIVEN
        CreateProfileRequest request = new CreateProfileRequest();
        request.setFirstName("Juan");
        request.setAge(30);
        request.setHeight(180.0);
        request.setWeight(80.0);
        request.setGender("MALE");
        request.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE); // x1.55
        request.setGoal(Goal.MAINTAIN_WEIGHT);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
            when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            UserProfile result = userProfileService.createProfile(request);

            // THEN
            assertNotNull(result);
            // TMB calculada: 88.362 + (13.397*80) + (4.799*180) - (5.677*30) ≈ 1853
            // Mantenimiento: 1853 * 1.55 ≈ 2872
            assertTrue(result.getTargetKcal() > 2800 && result.getTargetKcal() < 2900);
            assertEquals(160, result.getTargetProtein()); // 80kg * 2
            assertEquals(80, result.getTargetFat());     // 80kg * 1
            verify(weightLogRepository, times(1)).save(any());
        }
    }

    @Test
    void shouldThrowExceptionWhenUserIsUnderage() {
        // GIVEN
        CreateProfileRequest request = new CreateProfileRequest();
        request.setAge(17);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.createProfile(request);
        });
    }
}