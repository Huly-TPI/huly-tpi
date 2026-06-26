package com.huly.backend.domain.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.model.emotionalRecommendation.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;

/**
 * Mapper de dominio para el caso de uso de creacion de eventos emocionales.
 */
public class CreateEmotionalEventMapper {

    public CreateEmotionalEventCommand toModel(CreateEmotionalEventRequest request) {
        return new CreateEmotionalEventCommand(
                request.userId(),
                request.source(),
                request.inputText(),
                request.detectedEmotion(),
                request.confidence(),
                request.valence(),
                request.arousal(),
                request.dominance(),
                request.intensity(),
                request.userGoal(),
                request.generatedRecommendation(),
                request.recommendedActivityId(),
                request.chosenActivityId()
        );
    }

    public EmotionalEventResponse toResponse(EmotionalEvent event) {
        return new EmotionalEventResponse(
                event.getId(),
                event.getUserId(),
                event.getSource(),
                event.getInputText(),
                event.getDetectedEmotion(),
                event.getConfidence(),
                event.getValence(),
                event.getArousal(),
                event.getDominance(),
                event.getIntensity(),
                event.getUserGoal(),
                event.getGeneratedRecommendation(),
                event.getRecommendedActivityId(),
                event.getChosenActivityId(),
                event.getRecommendationDecision(),
                event.getFeedbackScore(),
                event.getFeedbackText(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
