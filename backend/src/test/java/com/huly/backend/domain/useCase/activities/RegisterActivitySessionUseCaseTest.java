package com.huly.backend.domain.useCase.activities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class RegisterActivitySessionUseCaseTest {

    @Mock
    private ActivitySessionRepository activitySessionRepository;

    @InjectMocks
    private RegisterActivitySessionUseCase registerActivitySessionUseCase;

    @Test
    void execute_shouldSaveActivitySession() {
        Long userId = 1L;
        ActivityType type = ActivityType.RESPIRACION;

        ActivitySession expectedSession = ActivitySession.builder()
                .id(100L)
                .userId(userId)
                .activityType(type)
                .createdAt(Instant.now())
                .build();

        when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(expectedSession);

        ActivitySession result = registerActivitySessionUseCase.execute(userId, type);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityType()).isEqualTo(type);
        verify(activitySessionRepository).save(any(ActivitySession.class));
    }
}
