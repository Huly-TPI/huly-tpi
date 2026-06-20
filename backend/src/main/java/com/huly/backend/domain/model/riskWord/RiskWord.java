package com.huly.backend.domain.model.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;
import lombok.*;

/**
 * Modelo de dominio que representa una palabra de riesgo emocional.
 * Estas palabras son utilizadas como contexto para el análisis de IA
 * a fin de detectar situaciones de riesgo en los mensajes de los usuarios.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RiskWord {

    /** Identificador único de la palabra de riesgo. */
    private Long id;

    /** Palabra o frase que representa un indicador de riesgo emocional. */
    private String word;

    /** Descripción opcional que explica el contexto o motivo del riesgo. */
    private String description;

    /** Nivel de severidad del riesgo asociado a esta palabra. */
    private RiskSeverity severity;

    /** Indica si la palabra de riesgo está activa y debe considerarse en el análisis. */
    private boolean active;
}
