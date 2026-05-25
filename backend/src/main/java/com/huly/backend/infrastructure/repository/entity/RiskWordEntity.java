package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.RiskSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad JPA que mapea la tabla {@code risk_word} en la base de datos.
 * Pertenece a la capa de infraestructura y es independiente del modelo de dominio
 * {@link com.huly.backend.domain.model.RiskWord}. La conversión entre ambas
 * representaciones la realiza {@link com.huly.backend.infrastructure.repository.jpaRepository.implementation.RiskWordRepositoryImpl}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "risk_word")
public class RiskWordEntity {

    /** Identificador autoincremental generado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Palabra o frase de riesgo. Valor único y no nulo en la tabla. */
    @Column(nullable = false, unique = true, length = 200)
    private String word;

    /** Descripción opcional que detalla el contexto del riesgo. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Nivel de severidad almacenado como texto en la columna {@code severity}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskSeverity severity;

    /** Indica si la palabra está activa. Por defecto {@code true} al crear. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Fecha y hora de creación del registro. Se asigna una sola vez al persistir. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Asigna automáticamente las marcas de tiempo de creación y última modificación
     * antes de que el registro sea persistido por primera vez.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    /**
     * Actualiza la marca de tiempo de última modificación cada vez que
     * el registro es actualizado en la base de datos.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
