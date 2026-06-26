package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.emotionalEvent.CreateEmotionalEventMapper;
import com.huly.backend.domain.model.emotionalRecommendation.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreateEmotionalEventUseCase {

    private final EmotionalEventRepository emotionalEventRepository;
    private final ActivityRepository activityRepository;
    private final CreateEmotionalEventMapper mapper;

    public EmotionalEventResponse execute(CreateEmotionalEventRequest request) {
        CreateEmotionalEventCommand command = mapper.toModel(request);
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
        return mapper.toResponse(emotionalEventRepository.save(event));
    }

    private void validate(CreateEmotionalEventCommand command) {
        if (command.source() == null) {
            throw new BusinessRuleException("source es obligatorio");
        }
        if (command.detectedEmotion() == null || command.detectedEmotion().isBlank()) {
            throw new BusinessRuleException("detectedEmotion es obligatorio");
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
            throw new BusinessRuleException(field + " debe estar entre " + min + " y " + max);
        }
    }

    private void validateActivityExists(Long activityId, String field) {
        if (activityId != null && !activityRepository.existsById(activityId)) {
            throw new BusinessRuleException(field + " no existe");
        }
    }
}
