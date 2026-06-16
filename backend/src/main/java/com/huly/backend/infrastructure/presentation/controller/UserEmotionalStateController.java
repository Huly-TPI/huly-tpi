package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.UserEmotionalState;
import com.huly.backend.domain.useCase.emotionalEvent.SaveUserEmotionalStateUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/emotional-states")
public class UserEmotionalStateController {

    private final SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase;

    @PostMapping
    public ResponseEntity<UserEmotionalState> save(@Valid @RequestBody UserEmotionalStateRequest request) {
        UserEmotionalState savedState = saveUserEmotionalStateUseCase.execute(
                request.userId(),
                request.valence(),
                request.arousal(),
                request.dominance(),
                request.intensity(),
                request.source()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedState);
    }
    
}
