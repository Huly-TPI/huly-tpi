package com.huly.backend.domain.dto.cloudRecommendation;

import java.util.List;

/**
 * Pedido de dominio para obtener una recomendacion a partir de pensamientos de nube.
 *
 * @param thoughts pensamientos del usuario.
 * @param userId   usuario que solicita la recomendacion (puede ser null).
 */
public record GetCloudRecommendationRequest(List<String> thoughts, Long userId) {
}
