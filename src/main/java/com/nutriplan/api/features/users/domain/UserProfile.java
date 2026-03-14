package com.nutriplan.api.features.users.domain;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nutriplan.api.features.users.domain.enums.ActivityLevel;
import com.nutriplan.api.features.users.domain.enums.Goal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usr_profiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "height_cm", nullable = false)
    private Double height;

    @Column(name = "weight_kg", nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal", nullable = false)
    private Goal goal;

    @Column(name = "target_kcal")
    private Integer targetKcal;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();

    // insertable/updatable = false para que el TRIGGER de Postgres tome el mando
    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.OffsetDateTime updatedAt;

    private Integer targetProtein;
    private Integer targetCarbs;
    private Integer targetFat;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<WeightLog> weightLogs;
    @Builder.Default
    private boolean isConfigured = false; // Nuevo campo para indicar si el perfil está configurado

}
