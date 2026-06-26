package com.huly.backend.domain.dto.cloudRecommendation;

/**
 * Respuesta de dominio con la recomendacion generada para una nube.
 */
public record GetCloudRecommendationResponse(
        String activityType,
        String actionId,
        String title,
        String description,
        String redirectUrl
) {
}
