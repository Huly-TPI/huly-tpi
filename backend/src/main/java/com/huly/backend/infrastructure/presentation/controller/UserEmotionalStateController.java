package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.emotionalEvent.SaveUserEmotionalStateUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateResponse;
import com.huly.backend.infrastructure.presentation.mapper.emotionalEvent.UserEmotionalStatePresentationMapper;
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
    private final UserEmotionalStatePresentationMapper userEmotionalStatePresentationMapper;

    @PostMapping
    public ResponseEntity<UserEmotionalStateResponse> save(@Valid @RequestBody UserEmotionalStateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userEmotionalStatePresentationMapper.toStateResponse(
                        saveUserEmotionalStateUseCase.execute(
                                userEmotionalStatePresentationMapper.toSaveRequest(request))));
    }

}
