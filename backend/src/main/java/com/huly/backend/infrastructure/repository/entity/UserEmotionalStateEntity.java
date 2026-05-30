package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_emotional_state")
public class UserEmotionalStateEntity {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private double valence;

    @Column(nullable = false)
    private double arousal;

    @Column(nullable = false)
    private double dominance;

    @Column(nullable = false)
    private double intensity;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private Instant timestamp;
}
