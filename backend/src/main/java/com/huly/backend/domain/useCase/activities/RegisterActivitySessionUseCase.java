package com.huly.backend.domain.useCase.activities;

import com.huly.backend.domain.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.domain.dto.activities.RegisterActivitySessionResponse;
import com.huly.backend.domain.mapper.activities.RegisterActivitySessionMapper;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterActivitySessionUseCase {

    private final ActivitySessionRepository activitySessionRepository;
    private final RegisterActivitySessionMapper mapper;

    public RegisterActivitySessionResponse execute(RegisterActivitySessionRequest request) {
        ActivitySession saved = activitySessionRepository.save(mapper.toModel(request));
        return mapper.toResponse(saved);
    }
}
