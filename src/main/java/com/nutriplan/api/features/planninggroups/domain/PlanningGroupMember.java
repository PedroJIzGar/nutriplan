package com.nutriplan.api.features.planninggroups.domain;

import java.util.UUID;

import com.nutriplan.api.features.planninggroups.domain.enums.PlanningGroupRole;
import com.nutriplan.api.features.users.domain.UserProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "planning_group_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_planning_group_member_group_user", columnNames = { "planning_group_id",
                "user_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningGroupMember {

    @Id
    @Builder.Default
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_group_id", nullable = false)
    private PlanningGroup planningGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanningGroupRole role;

    @Column(name = "portion_factor", nullable = false)
    private Double portionFactor;
}