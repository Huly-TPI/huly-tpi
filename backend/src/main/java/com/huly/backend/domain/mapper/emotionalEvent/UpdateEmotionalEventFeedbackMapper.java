package com.huly.backend.domain.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.UpdateEmotionalEventFeedbackCommand;

/**
 * Mapper de dominio para el caso de uso de actualizacion de feedback de un evento emocional.
 */
public class UpdateEmotionalEventFeedbackMapper {

    public UpdateEmotionalEventFeedbackCommand toModel(UpdateEmotionalEventFeedbackRequest request) {
        return new UpdateEmotionalEventFeedbackCommand(
                request.feedbackScore(),
                request.feedbackText()
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
