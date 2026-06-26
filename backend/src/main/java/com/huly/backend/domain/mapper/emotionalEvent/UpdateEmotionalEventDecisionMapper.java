package com.huly.backend.domain.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventDecisionRequest;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.UpdateRecommendationDecisionCommand;

/**
 * Mapper de dominio para el caso de uso de actualizacion de decision de recomendacion.
 */
public class UpdateEmotionalEventDecisionMapper {

    public UpdateRecommendationDecisionCommand toModel(UpdateEmotionalEventDecisionRequest request) {
        return new UpdateRecommendationDecisionCommand(
                request.decision(),
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
