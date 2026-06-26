package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventFeedbackMapper;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateEmotionalEventFeedbackUseCase {

    private final EmotionalEventRepository emotionalEventRepository;
    private final UpdateEmotionalEventFeedbackMapper mapper;

    public EmotionalEventResponse execute(UpdateEmotionalEventFeedbackRequest request) {
        Long eventId = request.eventId();
        UpdateEmotionalEventFeedbackCommand command = mapper.toModel(request);
        if (command.feedbackScore() != null && (command.feedbackScore() < 1 || command.feedbackScore() > 5)) {
            throw new BusinessRuleException("feedbackScore debe estar entre 1 y 5");
        }

        EmotionalEvent event = emotionalEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("EmotionalEvent", "id", eventId));

        EmotionalEvent updated = event.toBuilder()
                .feedbackScore(command.feedbackScore())
                .feedbackText(command.feedbackText())
                .updatedAt(Instant.now())
                .build();
        return mapper.toResponse(emotionalEventRepository.save(updated));
    }
}
