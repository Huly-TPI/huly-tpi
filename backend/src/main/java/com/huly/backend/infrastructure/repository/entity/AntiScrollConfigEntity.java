package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "antiscroll_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AntiScrollConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "default_pause_interval_minutes")
    private Integer defaultPauseIntervalMinutes;

    @Column(name = "terms_and_conditions")
    private String termsAndConditions;
}
