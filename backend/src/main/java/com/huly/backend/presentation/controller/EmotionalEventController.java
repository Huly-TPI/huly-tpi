package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.model.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.useCase.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.presentation.dto.emotionalEvent.EmotionalEventRequest;
import com.huly.backend.presentation.dto.emotionalEvent.EmotionalEventResponse;
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

/**
 * Controlador de eventos emocionales.
 * Un EmotionalEvent se crea automáticamente cuando el chatbot detecta un estado emocional
 * que requiere recomendación de actividad. También puede crearse manualmente.
 *
 * Ciclo de vida del evento:
 *  1. POST / → se crea el evento con la emoción detectada y la actividad recomendada.
 *  2. PATCH /{id}/decision → el usuario decide si acepta o rechaza la actividad recomendada.
 *  3. PATCH /{id}/feedback → el usuario califica la actividad una vez completada (1-5 estrellas + texto).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotional-events")
public class EmotionalEventController {

    private final CreateEmotionalEventUseCase createEmotionalEventUseCase;
    private final UpdateEmotionalEventDecisionUseCase updateDecisionUseCase;
    private final UpdateEmotionalEventFeedbackUseCase updateFeedbackUseCase;

    /** Registra un nuevo evento emocional. Valida rangos de confidence, valence, arousal, dominance e intensity. */
    @PostMapping
    public ResponseEntity<EmotionalEventResponse> create(@Valid @RequestBody EmotionalEventRequest request) {
        EmotionalEvent created = createEmotionalEventUseCase.execute(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(EmotionalEventResponse.from(created));
    }

    /**
     * Registra la decisión del usuario sobre la actividad recomendada.
     * ACCEPTED/REJECTED + la actividad que eligió finalmente (puede diferir de la recomendada).
     */
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

    /** Registra el feedback del usuario sobre la actividad (puntaje numérico + comentario libre). */
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

    /** Transforma el DTO del request en el Command de dominio para el caso de uso. */
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
