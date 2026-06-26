package com.huly.backend.infrastructure.presentation.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventDecisionRequest;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de eventos emocionales:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class EmotionalEventPresentationMapper {

    public CreateEmotionalEventRequest toCreateRequest(EmotionalEventRequest request) {
        return new CreateEmotionalEventRequest(
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

    public UpdateEmotionalEventDecisionRequest toDecisionRequest(Long eventId, EmotionalEventDecisionRequest request) {
        return new UpdateEmotionalEventDecisionRequest(
                eventId,
                request.decision(),
                request.chosenActivityId()
        );
    }

    public UpdateEmotionalEventFeedbackRequest toFeedbackRequest(Long eventId, EmotionalEventFeedbackRequest request) {
        return new UpdateEmotionalEventFeedbackRequest(
                eventId,
                request.feedbackScore(),
                request.feedbackText()
        );
    }

    public EmotionalEventResponse toEventResponse(com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse response) {
        return new EmotionalEventResponse(
                response.id(),
                response.userId(),
                response.source(),
                response.inputText(),
                response.detectedEmotion(),
                response.confidence(),
                response.valence(),
                response.arousal(),
                response.dominance(),
                response.intensity(),
                response.userGoal(),
                response.generatedRecommendation(),
                response.recommendedActivityId(),
                response.chosenActivityId(),
                response.recommendationDecision(),
                response.feedbackScore(),
                response.feedbackText(),
                response.createdAt(),
                response.updatedAt()
        );
    }
}
