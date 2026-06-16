package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.LanternStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lantern_thought")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanternThoughtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @Column(name = "text", nullable = false, length = 100)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LanternStatus status;

    @Column(name = "worked_on", nullable = false)
    private boolean workedOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
