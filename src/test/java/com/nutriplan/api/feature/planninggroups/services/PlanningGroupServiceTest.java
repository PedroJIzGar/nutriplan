package com.nutriplan.api.feature.planninggroups.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nutriplan.api.features.planninggroups.domain.PlanningGroup;
import com.nutriplan.api.features.planninggroups.domain.PlanningGroupMember;
import com.nutriplan.api.features.planninggroups.domain.enums.PlanningGroupRole;
import com.nutriplan.api.features.planninggroups.domain.repository.PlanningGroupMemberRepository;
import com.nutriplan.api.features.planninggroups.domain.repository.PlanningGroupRepository;
import com.nutriplan.api.features.planninggroups.dto.AddPlanningGroupMemberRequest;
import com.nutriplan.api.features.planninggroups.dto.CreatePlanningGroupRequest;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupMemberResponse;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupResponse;
import com.nutriplan.api.features.planninggroups.mapper.PlanningGroupMapper;
import com.nutriplan.api.features.planninggroups.services.PlanningGroupService;
import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;
import com.nutriplan.api.features.users.domain.repository.UserProfileRepository;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class PlanningGroupServiceTest {

    @Mock
    private PlanningGroupRepository planningGroupRepository;

    @Mock
    private PlanningGroupMemberRepository planningGroupMemberRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PlanningGroupMapper planningGroupMapper;

    private PlanningGroupService buildService() {
        return new PlanningGroupService(
                planningGroupRepository,
                planningGroupMemberRepository,
                userProfileRepository,
                planningGroupMapper);
    }

    @Test
    @DisplayName("createGroup should create group and owner membership")
    void createGroupShouldCreateGroupAndOwnerMembership() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UserProfile currentUser = buildUser(currentUserId, "Hugo", "Rios", "admin@nutriplan.com");

        CreatePlanningGroupRequest request = CreatePlanningGroupRequest.builder()
                .name("Casa Hugo")
                .build();

        PlanningGroup savedGroup = PlanningGroup.builder()
                .id(UUID.randomUUID())
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        PlanningGroupResponse response = buildResponse(savedGroup.getId(), currentUserId);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(userProfileRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
            when(planningGroupRepository.save(any(PlanningGroup.class))).thenReturn(savedGroup);
            when(planningGroupRepository.findByIdWithMembers(savedGroup.getId())).thenReturn(Optional.of(savedGroup));
            when(planningGroupMapper.toResponse(savedGroup)).thenReturn(response);

            PlanningGroupResponse result = service.createGroup(request);

            assertNotNull(result);
            assertEquals("Casa Hugo", result.getName());

            verify(planningGroupRepository).save(any(PlanningGroup.class));
            verify(planningGroupMemberRepository).save(any(PlanningGroupMember.class));
            verify(planningGroupRepository).findByIdWithMembers(savedGroup.getId());
        }
    }

    @Test
    @DisplayName("getMyGroups should return user groups")
    void getMyGroupsShouldReturnUserGroups() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        PlanningGroup group = PlanningGroup.builder()
                .id(UUID.randomUUID())
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        PlanningGroupResponse response = buildResponse(group.getId(), currentUserId);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupRepository.findAllByUserIdWithMembers(currentUserId))
                    .thenReturn(List.of(group));
            when(planningGroupMapper.toResponse(group)).thenReturn(response);

            List<PlanningGroupResponse> result = service.getMyGroups();

            assertEquals(1, result.size());
            assertEquals("Casa Hugo", result.get(0).getName());
        }
    }

    @Test
    @DisplayName("getGroupById should throw when current user is not member")
    void getGroupByIdShouldThrowWhenUserIsNotMember() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupMemberRepository.existsByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId))
                    .thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> service.getGroupById(groupId));
        }
    }

    @Test
    @DisplayName("addMember should add member when current user is owner")
    void addMemberShouldWorkWhenCurrentUserIsOwner() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        PlanningGroup group = PlanningGroup.builder()
                .id(groupId)
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        UserProfile newUser = buildUser(newUserId, "Ana", "Lopez", "ana@nutriplan.com");

        PlanningGroupMember ownerMembership = PlanningGroupMember.builder()
                .planningGroup(group)
                .userProfile(buildUser(currentUserId, "Hugo", "Rios", "admin@nutriplan.com"))
                .role(PlanningGroupRole.OWNER)
                .portionFactor(1.0)
                .build();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(newUserId)
                .portionFactor(0.8)
                .build();

        PlanningGroupResponse response = buildResponse(groupId, currentUserId);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(planningGroupMemberRepository.findByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId))
                    .thenReturn(Optional.of(ownerMembership));
            when(planningGroupMemberRepository.existsByPlanningGroupIdAndUserProfileUserId(groupId, newUserId))
                    .thenReturn(false);
            when(userProfileRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
            when(planningGroupRepository.findByIdWithMembers(groupId)).thenReturn(Optional.of(group));
            when(planningGroupMapper.toResponse(group)).thenReturn(response);

            PlanningGroupResponse result = service.addMember(groupId, request);

            assertNotNull(result);
            verify(planningGroupMemberRepository).save(any(PlanningGroupMember.class));
        }
    }

    @Test
    @DisplayName("addMember should throw conflict when current user is not owner")
    void addMemberShouldThrowWhenCurrentUserIsNotOwner() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        PlanningGroup group = PlanningGroup.builder()
                .id(groupId)
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        PlanningGroupMember memberMembership = PlanningGroupMember.builder()
                .planningGroup(group)
                .userProfile(buildUser(currentUserId, "Hugo", "Rios", "admin@nutriplan.com"))
                .role(PlanningGroupRole.MEMBER)
                .portionFactor(1.0)
                .build();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(UUID.randomUUID())
                .portionFactor(0.8)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(planningGroupMemberRepository.findByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId))
                    .thenReturn(Optional.of(memberMembership));

            assertThrows(ConflictException.class, () -> service.addMember(groupId, request));
        }
    }

    @Test
    @DisplayName("addMember should throw conflict when user already belongs to group")
    void addMemberShouldThrowWhenUserAlreadyBelongsToGroup() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        PlanningGroup group = PlanningGroup.builder()
                .id(groupId)
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        PlanningGroupMember ownerMembership = PlanningGroupMember.builder()
                .planningGroup(group)
                .userProfile(buildUser(currentUserId, "Hugo", "Rios", "admin@nutriplan.com"))
                .role(PlanningGroupRole.OWNER)
                .portionFactor(1.0)
                .build();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(newUserId)
                .portionFactor(0.8)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(planningGroupMemberRepository.findByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId))
                    .thenReturn(Optional.of(ownerMembership));
            when(planningGroupMemberRepository.existsByPlanningGroupIdAndUserProfileUserId(groupId, newUserId))
                    .thenReturn(true);

            assertThrows(ConflictException.class, () -> service.addMember(groupId, request));
        }
    }

    @Test
    @DisplayName("addMember should throw not found when target user does not exist")
    void addMemberShouldThrowWhenTargetUserDoesNotExist() {
        PlanningGroupService service = buildService();

        UUID currentUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        PlanningGroup group = PlanningGroup.builder()
                .id(groupId)
                .name("Casa Hugo")
                .createdByUserId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        PlanningGroupMember ownerMembership = PlanningGroupMember.builder()
                .planningGroup(group)
                .userProfile(buildUser(currentUserId, "Hugo", "Rios", "admin@nutriplan.com"))
                .role(PlanningGroupRole.OWNER)
                .portionFactor(1.0)
                .build();

        AddPlanningGroupMemberRequest request = AddPlanningGroupMemberRequest.builder()
                .userId(newUserId)
                .portionFactor(0.8)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(planningGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(planningGroupMemberRepository.findByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId))
                    .thenReturn(Optional.of(ownerMembership));
            when(planningGroupMemberRepository.existsByPlanningGroupIdAndUserProfileUserId(groupId, newUserId))
                    .thenReturn(false);
            when(userProfileRepository.findById(newUserId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.addMember(groupId, request));
        }
    }

    private UserProfile buildUser(UUID userId, String firstName, String lastName, String email) {
        return UserProfile.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .age(30)
                .gender("MALE")
                .height(180.0)
                .weight(80.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goal(Goal.LOSE_WEIGHT)
                .targetKcal(2200)
                .targetProtein(160)
                .targetCarbs(200)
                .targetFat(70)
                .isConfigured(true)
                .build();
    }

    private PlanningGroupResponse buildResponse(UUID groupId, UUID createdByUserId) {
        return PlanningGroupResponse.builder()
                .id(groupId)
                .name("Casa Hugo")
                .createdByUserId(createdByUserId)
                .createdAt(OffsetDateTime.now())
                .members(List.of(
                        PlanningGroupMemberResponse.builder()
                                .userId(createdByUserId)
                                .firstName("Hugo")
                                .lastName("Rios")
                                .email("admin@nutriplan.com")
                                .role(PlanningGroupRole.OWNER)
                                .portionFactor(1.0)
                                .build()))
                .build();
    }
}