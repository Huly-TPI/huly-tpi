package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.PlantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_plant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user", nullable = false)
    private AppUserEntity appUser;

    @Column(name = "plant_number", nullable = false)
    private Integer plantNumber;

    @Column(name = "required_goals", nullable = false)
    private Integer requiredGoals;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PlantStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
