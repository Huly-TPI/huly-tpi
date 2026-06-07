package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

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

    private void validate(CreateEmotionalEventCommand command) {
        if (command.source() == null) {
            throw new BadRequestException("source es obligatorio");
        }
        if (command.detectedEmotion() == null || command.detectedEmotion().isBlank()) {
            throw new BadRequestException("detectedEmotion es obligatorio");
        }
        validateNullableRange("confidence", command.confidence(), 0.0, 1.0);
        validateNullableRange("valence", command.valence(), Vad.MIN_VALUE, Vad.MAX_VALUE);
        validateNullableRange("arousal", command.arousal(), Vad.MIN_VALUE, Vad.MAX_VALUE);
        validateNullableRange("dominance", command.dominance(), Vad.MIN_VALUE, Vad.MAX_VALUE);
        validateNullableRange("intensity", command.intensity(), 0.0, 1.0);
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
