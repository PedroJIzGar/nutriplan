package com.nutriplan.api.features.planninggroups.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.planninggroups.domain.PlanningGroup;
import com.nutriplan.api.features.planninggroups.domain.PlanningGroupMember;
import com.nutriplan.api.features.planninggroups.domain.enums.PlanningGroupRole;
import com.nutriplan.api.features.planninggroups.domain.repository.PlanningGroupMemberRepository;
import com.nutriplan.api.features.planninggroups.domain.repository.PlanningGroupRepository;
import com.nutriplan.api.features.planninggroups.dto.AddPlanningGroupMemberRequest;
import com.nutriplan.api.features.planninggroups.dto.CreatePlanningGroupRequest;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupResponse;
import com.nutriplan.api.features.planninggroups.mapper.PlanningGroupMapper;
import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.domain.repository.UserProfileRepository;
import com.nutriplan.api.shared.exception.ConflictException;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanningGroupService {

    private final PlanningGroupRepository planningGroupRepository;
    private final PlanningGroupMemberRepository planningGroupMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final PlanningGroupMapper planningGroupMapper;

    @Transactional
    public PlanningGroupResponse createGroup(CreatePlanningGroupRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe perfil para el usuario autenticado"));

        PlanningGroup group = PlanningGroup.builder()
                .name(request.getName())
                .createdByUserId(currentUserId)
                .build();

        PlanningGroup savedGroup = planningGroupRepository.save(group);

        PlanningGroupMember ownerMember = PlanningGroupMember.builder()
                .planningGroup(savedGroup)
                .userProfile(currentUser)
                .role(PlanningGroupRole.OWNER)
                .portionFactor(1.0)
                .build();

        planningGroupMemberRepository.save(ownerMember);

        PlanningGroup groupWithMembers = planningGroupRepository.findByIdWithMembers(savedGroup.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado tras su creación"));

        return planningGroupMapper.toResponse(groupWithMembers);
    }

    @Transactional(readOnly = true)
    public List<PlanningGroupResponse> getMyGroups() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        List<PlanningGroup> groups = planningGroupRepository.findAllByUserIdWithMembers(currentUserId);

        return groups.stream()
                .map(planningGroupMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanningGroupResponse getGroupById(UUID groupId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        boolean isMember = planningGroupMemberRepository
                .existsByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId);

        if (!isMember) {
            throw new ResourceNotFoundException("Grupo no encontrado para el usuario autenticado");
        }

        PlanningGroup group = planningGroupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));

        return planningGroupMapper.toResponse(group);
    }

    @Transactional
    public PlanningGroupResponse addMember(UUID groupId, AddPlanningGroupMemberRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        PlanningGroup group = planningGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));

        PlanningGroupMember currentMembership = planningGroupMemberRepository
                .findByPlanningGroupIdAndUserProfileUserId(groupId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado para el usuario autenticado"));

        if (currentMembership.getRole() != PlanningGroupRole.OWNER) {
            throw new ConflictException("Solo el propietario del grupo puede añadir miembros");
        }

        if (planningGroupMemberRepository.existsByPlanningGroupIdAndUserProfileUserId(groupId, request.getUserId())) {
            throw new ConflictException("El usuario ya pertenece a este grupo");
        }

        UserProfile newMemberUser = userProfileRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario a añadir no existe"));

        PlanningGroupMember newMember = PlanningGroupMember.builder()
                .planningGroup(group)
                .userProfile(newMemberUser)
                .role(PlanningGroupRole.MEMBER)
                .portionFactor(request.getPortionFactor())
                .build();

        planningGroupMemberRepository.save(newMember);

        PlanningGroup groupWithMembers = planningGroupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado tras añadir miembro"));

        return planningGroupMapper.toResponse(groupWithMembers);
    }
}