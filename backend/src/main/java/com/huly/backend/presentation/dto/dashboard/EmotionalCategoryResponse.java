package com.huly.backend.presentation.dto.dashboard;

/**
 * DTO de salida para una categoría emocional del panel de chatbot.
 *
 * @param name     nombre de la categoría
 * @param flows    cantidad de flujos configurados
 * @param detect   porcentaje de detección
 * @param severity nivel de severidad: ALTA, MEDIA o BAJA
 */
public record EmotionalCategoryResponse(
        String name,
        int flows,
        int detect,
        String severity
) {}
