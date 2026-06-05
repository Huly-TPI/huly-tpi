package com.huly.backend.infrastructure.presentation.dto.dashboard;

/**
 * DTO de salida para un registro de entrenamiento del sistema.
 *
 * @param message    mensaje del usuario tal como fue recibido
 * @param emotion    emoción detectada por la IA
 * @param confidence porcentaje de confianza de la detección (0–100)
 */
public record TrainingLogResponse(
        String message,
        String emotion,
        double confidence
) {}
