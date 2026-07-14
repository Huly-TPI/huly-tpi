package com.huly.backend.infrastructure.presentation.dto.dashboard;

/**
 * DTO de salida para una actividad de la biblioteca del chatbot.
 *
 * @param name nombre de la actividad
 * @param pct  porcentaje de uso/completitud
 */
public record ActivityResponse(
        String name,
        String type,
        int pct
) {}
