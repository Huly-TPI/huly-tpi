package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mandala_plan_entitlement",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_code", "mandala_id"}))
public class MandalaPlanEntitlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_code", nullable = false, length = 50)
    private String planCode;

    @Column(name = "mandala_id", nullable = false, length = 100)
    private String mandalaId;
}
