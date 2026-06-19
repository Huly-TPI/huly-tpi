package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.emotionalRecommendation.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.model.emotionalRecommendation.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotional-events")
public class EmotionalEventController {

    private final CreateEmotionalEventUseCase createEmotionalEventUseCase;
    private final UpdateEmotionalEventDecisionUseCase updateDecisionUseCase;
    private final UpdateEmotionalEventFeedbackUseCase updateFeedbackUseCase;

    @PostMapping
    public ResponseEntity<EmotionalEventResponse> create(@Valid @RequestBody EmotionalEventRequest request) {
        EmotionalEvent created = createEmotionalEventUseCase.execute(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(EmotionalEventResponse.from(created));
    }

    @PatchMapping("/{id}/decision")
    public ResponseEntity<EmotionalEventResponse> updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody EmotionalEventDecisionRequest request
    ) {
        EmotionalEvent updated = updateDecisionUseCase.execute(
                id,
                new UpdateRecommendationDecisionCommand(request.decision(), request.chosenActivityId())
        );
        return ResponseEntity.ok(EmotionalEventResponse.from(updated));
    }

    @PatchMapping("/{id}/feedback")
    public ResponseEntity<EmotionalEventResponse> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody EmotionalEventFeedbackRequest request
    ) {
        EmotionalEvent updated = updateFeedbackUseCase.execute(
                id,
                new UpdateEmotionalEventFeedbackCommand(request.feedbackScore(), request.feedbackText())
        );
        return ResponseEntity.ok(EmotionalEventResponse.from(updated));
    }

    private CreateEmotionalEventCommand toCommand(EmotionalEventRequest request) {
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
}
