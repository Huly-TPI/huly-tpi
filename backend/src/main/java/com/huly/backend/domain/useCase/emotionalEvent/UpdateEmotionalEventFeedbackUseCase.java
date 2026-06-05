package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UpdateEmotionalEventFeedbackUseCase {

    private final EmotionalEventRepository emotionalEventRepository;

    public EmotionalEvent execute(Long eventId, UpdateEmotionalEventFeedbackCommand command) {
        if (command.feedbackScore() != null && (command.feedbackScore() < 1 || command.feedbackScore() > 5)) {
            throw new BadRequestException("feedbackScore debe estar entre 1 y 5");
        }

        EmotionalEvent event = emotionalEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("EmotionalEvent", "id", eventId));

        EmotionalEvent updated = event.toBuilder()
                .feedbackScore(command.feedbackScore())
                .feedbackText(command.feedbackText())
                .updatedAt(Instant.now())
                .build();
        return emotionalEventRepository.save(updated);
    }
}
