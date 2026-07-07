package com.huly.backend.domain.useCase.activities;

import com.huly.backend.domain.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.domain.dto.activities.RegisterActivitySessionResponse;
import com.huly.backend.domain.mapper.activities.RegisterActivitySessionMapper;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterActivitySessionUseCase {

    private final ActivitySessionRepository activitySessionRepository;
    private final MandalaProgressRepository mandalaProgressRepository;
    private final RegisterActivitySessionMapper mapper;

    public RegisterActivitySessionResponse execute(RegisterActivitySessionRequest request) {
        ActivitySession saved = activitySessionRepository.save(mapper.toModel(request));
        if (request.activityType() == ActivityType.MANDALA && request.contextId() != null && !request.contextId().isBlank()) {
            mandalaProgressRepository.markSessionRegistered(request.userId(), request.contextId());
        }
        return mapper.toResponse(saved);
    }
}
