package com.huly.backend.domain.model.enums;

/**
 * Nivel de severidad asociado a una palabra de riesgo emocional.
 * Se utiliza para priorizar el análisis de la IA según la gravedad
 * del indicador detectado en el mensaje del usuario.
 */
public enum RiskSeverity {

    /** Riesgo bajo: indicador de posible malestar leve o estrés moderado. */
    LOW,

    /** Riesgo medio: indicador de angustia emocional que requiere atención. */
    MEDIUM,

    /** Riesgo alto: indicador de peligro inmediato que exige intervención urgente. */
    HIGH
}
