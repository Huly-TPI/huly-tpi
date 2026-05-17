package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "breathing_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreathingSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technique_id")
    private BreathingTechniques breathingTechnique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user")
    private AppUser appUser;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}
