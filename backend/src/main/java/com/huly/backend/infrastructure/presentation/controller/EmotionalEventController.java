package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.infrastructure.presentation.mapper.emotionalEvent.EmotionalEventPresentationMapper;
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
    private final EmotionalEventPresentationMapper emotionalEventPresentationMapper;

    @PostMapping
    public ResponseEntity<EmotionalEventResponse> create(@Valid @RequestBody EmotionalEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                emotionalEventPresentationMapper.toEventResponse(
                        createEmotionalEventUseCase.execute(
                                emotionalEventPresentationMapper.toCreateRequest(request))));
    }

    @PatchMapping("/{id}/decision")
    public ResponseEntity<EmotionalEventResponse> updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody EmotionalEventDecisionRequest request
    ) {
        return ResponseEntity.ok(
                emotionalEventPresentationMapper.toEventResponse(
                        updateDecisionUseCase.execute(
                                emotionalEventPresentationMapper.toDecisionRequest(id, request))));
    }

    @PatchMapping("/{id}/feedback")
    public ResponseEntity<EmotionalEventResponse> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody EmotionalEventFeedbackRequest request
    ) {
        return ResponseEntity.ok(
                emotionalEventPresentationMapper.toEventResponse(
                        updateFeedbackUseCase.execute(
                                emotionalEventPresentationMapper.toFeedbackRequest(id, request))));
    }
}
