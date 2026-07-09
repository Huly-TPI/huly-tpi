package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "breathing_techniques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreathingTechniquesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "inhale_seconds")
    private Integer inhaleSeconds;

    @Column(name = "hold_seconds")
    private Integer holdSeconds;

    @Column(name = "exhale_seconds")
    private Integer exhaleSeconds;

    @Column(name = "rounds_interval")
    private Integer roundsInterval;

    @Column(name = "rounds")
    private Integer rounds;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "breathingTechnique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreathingSessionsEntity> breathingSessions;

}
