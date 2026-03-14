package com.nutriplan.api.features.users.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usr_weight_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Double weight;
    private LocalDateTime logDate;

    @ManyToOne
    @JoinColumn(name = "user_id") // El nombre de la columna en la DB
    @JsonBackReference
    private UserProfile user;

}