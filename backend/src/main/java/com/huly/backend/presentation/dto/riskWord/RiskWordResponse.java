package com.huly.backend.presentation.dto.riskWord;

/**
 * DTO de salida que representa una palabra de riesgo en las respuestas de la API.
 * La severidad se devuelve como texto para desacoplar al cliente del enum interno.
 *
 * @param id          identificador único del registro
 * @param word        palabra o frase de riesgo
 * @param description descripción del contexto de riesgo (puede ser {@code null})
 * @param severity    nivel de severidad como texto: {@code LOW}, {@code MEDIUM} o {@code HIGH}
 * @param active      indica si la palabra está activa en el sistema
 */
public record RiskWordResponse(
        Long id,
        String word,
        String description,
        String severity,
        boolean active
) {}
