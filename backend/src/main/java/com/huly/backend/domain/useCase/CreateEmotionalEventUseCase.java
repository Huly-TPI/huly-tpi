package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Caso de uso: registrar un evento emocional detectado por el sistema o el usuario.
 * Los eventos emocionales son el núcleo del sistema de seguimiento de bienestar.
 * Cada evento captura:
 *  - La emoción detectada y su intensidad (modelo VAD: Valence, Arousal, Dominance)
 *  - La fuente (CHATBOT, MANUAL, etc.)
 *  - La actividad recomendada y la que finalmente eligió el usuario
 *
 * El modelo VAD es un estándar psicológico para representar emociones en tres dimensiones:
 *  - Valence: positivo (felicidad) vs negativo (tristeza) [-1.0, 1.0]
 *  - Arousal: activado (ansiedad) vs tranquilo (calma) [-1.0, 1.0]
 *  - Dominance: control (dominio) vs sin control (sumisión) [-1.0, 1.0]
 */
@Service
@RequiredArgsConstructor
public class CreateEmotionalEventUseCase {

    private final EmotionalEventRepository emotionalEventRepository;
    private final ActivityRepository activityRepository;

    public EmotionalEvent execute(CreateEmotionalEventCommand command) {
        validate(command);
        Instant now = Instant.now();
        EmotionalEvent event = EmotionalEvent.builder()
                .userId(command.userId())
                .source(command.source())
                .inputText(command.inputText())
                .detectedEmotion(command.detectedEmotion())
                .confidence(command.confidence())
                .valence(command.valence())
                .arousal(command.arousal())
                .dominance(command.dominance())
                .intensity(command.intensity())
                .userGoal(command.userGoal())
                .generatedRecommendation(command.generatedRecommendation())
                .recommendedActivityId(command.recommendedActivityId())
                .chosenActivityId(command.chosenActivityId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return emotionalEventRepository.save(event);
    }

    /** Valida campos obligatorios y rangos numéricos de las métricas emocionales. */
    private void validate(CreateEmotionalEventCommand command) {
        if (command.source() == null) {
            throw new BadRequestException("source es obligatorio");
        }
        if (command.detectedEmotion() == null || command.detectedEmotion().isBlank()) {
            throw new BadRequestException("detectedEmotion es obligatorio");
        }
        // confidence e intensity son probabilidades: [0.0, 1.0]
        validateNullableRange("confidence", command.confidence(), 0.0, 1.0);
        validateNullableRange("intensity", command.intensity(), 0.0, 1.0);
        // Dimensiones VAD: [-1.0, 1.0] (negativos y positivos)
        validateNullableRange("valence", command.valence(), -1.0, 1.0);
        validateNullableRange("arousal", command.arousal(), -1.0, 1.0);
        validateNullableRange("dominance", command.dominance(), -1.0, 1.0);
        // Verifica que las actividades referenciadas existen en BD
        validateActivityExists(command.recommendedActivityId(), "recommendedActivityId");
        validateActivityExists(command.chosenActivityId(), "chosenActivityId");
    }

    private void validateNullableRange(String field, Double value, double min, double max) {
        if (value != null && (value < min || value > max)) {
            throw new BadRequestException(field + " debe estar entre " + min + " y " + max);
        }
    }

    private void validateActivityExists(Long activityId, String field) {
        if (activityId != null && !activityRepository.existsById(activityId)) {
            throw new BadRequestException(field + " no existe");
        }
    }
}
