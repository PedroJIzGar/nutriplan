package com.nutriplan.api.features.planninggroups.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nutriplan.api.features.planninggroups.domain.PlanningGroup;
import com.nutriplan.api.features.planninggroups.domain.PlanningGroupMember;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupMemberResponse;
import com.nutriplan.api.features.planninggroups.dto.PlanningGroupResponse;

@Mapper(componentModel = "spring")
public interface PlanningGroupMapper {

    @Mapping(target = "members", source = "members")
    PlanningGroupResponse toResponse(PlanningGroup group);

    List<PlanningGroupResponse> toResponseList(List<PlanningGroup> groups);

    @Mapping(target = "userId", source = "userProfile.userId")
    @Mapping(target = "firstName", source = "userProfile.firstName")
    @Mapping(target = "lastName", source = "userProfile.lastName")
    @Mapping(target = "email", source = "userProfile.email")
    PlanningGroupMemberResponse toMemberResponse(PlanningGroupMember member);

    List<PlanningGroupMemberResponse> toMemberResponseList(List<PlanningGroupMember> members);
}