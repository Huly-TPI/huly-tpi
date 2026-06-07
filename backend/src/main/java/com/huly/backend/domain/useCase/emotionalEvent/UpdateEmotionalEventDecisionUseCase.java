package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UpdateEmotionalEventDecisionUseCase {

    private final EmotionalEventRepository emotionalEventRepository;
    private final ActivityRepository activityRepository;
    private final UserVectorMemoryService userVectorMemoryService;

    public EmotionalEvent execute(Long eventId, UpdateRecommendationDecisionCommand command) {
        if (command.decision() == null) {
            throw new BusinessRuleException("decision es obligatoria");
        }

        EmotionalEvent event = emotionalEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("EmotionalEvent", "id", eventId));

        Long chosenActivityId = resolveChosenActivityId(event, command);
        if (chosenActivityId != null && !activityRepository.existsById(chosenActivityId)) {
            throw new BusinessRuleException("chosenActivityId no existe");
        }

        EmotionalEvent updated = event.toBuilder()
                .recommendationDecision(command.decision())
                .chosenActivityId(chosenActivityId)
                .updatedAt(Instant.now())
                .build();
        EmotionalEvent saved = emotionalEventRepository.save(updated);
        userVectorMemoryService.rememberActivityRecommendationDecision(saved);
        return saved;
    }

    private Long resolveChosenActivityId(EmotionalEvent event, UpdateRecommendationDecisionCommand command) {
        if (command.decision() == RecommendationDecision.IGNORED) {
            return null;
        }
        if (command.decision() == RecommendationDecision.ACCEPTED) {
            return command.chosenActivityId() != null ? command.chosenActivityId() : event.getRecommendedActivityId();
        }
        if (command.chosenActivityId() == null) {
            throw new BusinessRuleException("chosenActivityId es obligatorio para CHOSE_OTHER");
        }
        return command.chosenActivityId();
    }
}
