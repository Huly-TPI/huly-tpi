package com.huly.backend.domain.useCase.activities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.domain.dto.activities.RegisterActivitySessionResponse;
import com.huly.backend.domain.mapper.activities.RegisterActivitySessionMapper;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class RegisterActivitySessionUseCaseTest {

    @Mock
    private ActivitySessionRepository activitySessionRepository;
    @Mock
    private MandalaProgressRepository mandalaProgressRepository;

    private RegisterActivitySessionUseCase registerActivitySessionUseCase;

    @BeforeEach
    void setUp() {
        registerActivitySessionUseCase = new RegisterActivitySessionUseCase(
                activitySessionRepository,
                mandalaProgressRepository,
                new RegisterActivitySessionMapper());
    }

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

        RegisterActivitySessionResponse result = registerActivitySessionUseCase.execute(
                new RegisterActivitySessionRequest(userId, type, null));

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.activityType()).isEqualTo(type);
        verify(activitySessionRepository).save(any(ActivitySession.class));
    }

    @Test
    void execute_shouldMarkMandalaSessionWhenContextIsPresent() {
        Long userId = 1L;
        ActivitySession expectedSession = ActivitySession.builder()
                .id(200L)
                .userId(userId)
                .activityType(ActivityType.MANDALA)
                .createdAt(Instant.now())
                .build();

        when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(expectedSession);

        registerActivitySessionUseCase.execute(
                new RegisterActivitySessionRequest(userId, ActivityType.MANDALA, "mandala-01"));

        verify(mandalaProgressRepository).markSessionRegistered(userId, "mandala-01");
    }
}
