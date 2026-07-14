package com.huly.backend.infrastructure.presentation.dto.dashboard;

/**
 * DTO de salida para una categoría emocional del panel de chatbot.
 *
 * @param name       nombre de la categoría
 * @param detections cantidad de detecciones/ocurrencias
 * @param detect     porcentaje de detección
 * @param severity   nivel de severidad: ALTA, MEDIA o BAJA
 */
public record EmotionalCategoryResponse(
        String name,
        int detections,
        int detect,
        String severity
) {}
