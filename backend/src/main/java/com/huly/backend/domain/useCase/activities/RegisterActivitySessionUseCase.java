package com.huly.backend.domain.useCase.activities;

import com.huly.backend.domain.model.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterActivitySessionUseCase {

    private final ActivitySessionRepository activitySessionRepository;

    public ActivitySession execute(Long userId, ActivityType activityType) {
        ActivitySession session = ActivitySession.builder()
                .userId(userId)
                .activityType(activityType)
                .createdAt(Instant.now())
                .build();

        return activitySessionRepository.save(session);
    }
}
