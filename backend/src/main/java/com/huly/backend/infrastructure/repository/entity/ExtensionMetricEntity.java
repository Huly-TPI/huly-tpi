package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "extension_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtensionMetricEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user", nullable = false)
    private AppUserEntity appUser;

    @Column(nullable = false)
    private String domain;

    @Column(name = "active_seconds")
    private int activeSeconds;

    @Column(name = "scroll_count")
    private int scrollCount;

    @Column(name = "modals_shown")
    private int modalsShown;

    private int redirects;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
