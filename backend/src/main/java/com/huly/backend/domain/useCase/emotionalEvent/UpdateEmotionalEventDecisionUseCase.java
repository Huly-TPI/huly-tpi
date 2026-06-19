package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;

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
        if (saved != null && saved.getUserId() != null && saved.getRecommendationDecision() != null) {
            RecommendationDecision decision = saved.getRecommendationDecision();
            String decisionText = decision == RecommendationDecision.ACCEPTED ? "acepto"
                                : decision == RecommendationDecision.IGNORED ? "rechazo"
                                : "eligio otra actividad para";
            String recText = saved.getGeneratedRecommendation() != null ? saved.getGeneratedRecommendation() : "";
            String recommendedId = saved.getRecommendedActivityId() != null ? saved.getRecommendedActivityId().toString() : "";
            String chosenId = saved.getChosenActivityId() != null ? saved.getChosenActivityId().toString() : "";
            String eventIdStr = saved.getId() != null ? saved.getId().toString() : "";

            String contentStr = "El usuario %s la recomendacion de actividad. Actividad recomendada id: %s. Actividad elegida id: %s. Recomendacion: %s."
                    .formatted(decisionText, recommendedId, chosenId, recText);

            userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                    saved.getUserId(),
                    VectorMemorySource.CHATBOT,
                    saved.getId() != null ? saved.getId().toString() : saved.getUserId().toString(),
                    "ACTIVITY_RECOMMENDATION_DECISION",
                    "ACTIVITY_RECOMMENDATION_DECISION",
                    contentStr,
                    null,
                    saved.getId() != null ? saved.getId().toString() : null,
                    Map.of(
                            "createdFrom", "USER_MESSAGE",
                            "feature", "CHATBOT_ACTIVITY_DECISION",
                            "decision", decision.name(),
                            "recommendedActivityId", recommendedId,
                            "chosenActivityId", chosenId,
                            "emotionalEventId", eventIdStr
                    )
            ));
        }
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
