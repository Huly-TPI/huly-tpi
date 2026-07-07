package com.huly.backend.domain.useCase.activities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
class RegisterActivitySessionUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final String MANDALA_CONTEXT = "mandala-01";

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
    @DisplayName("Guarda la sesión de actividad y devuelve la respuesta mapeada")
    void registerShouldSaveActivitySession() {
        // --- arrange ---
        givenSavedSession(100L, USER_ID, ActivityType.BREATHING);

        // --- act ---
        RegisterActivitySessionResponse result = register(USER_ID, ActivityType.BREATHING, null);

        // --- assert ---
        thenSessionSaved(result, 100L, USER_ID, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Marca la sesión de mandala cuando el contexto está presente")
    void registerShouldMarkMandalaSessionWhenContextIsPresent() {
        // --- arrange ---
        givenSavedSession(200L, USER_ID, ActivityType.MANDALA);

        // --- act ---
        register(USER_ID, ActivityType.MANDALA, MANDALA_CONTEXT);

        // --- assert ---
        thenMandalaSessionMarked(USER_ID, MANDALA_CONTEXT);
    }

    @Test
    @DisplayName("No marca la sesión de mandala cuando el contexto es nulo")
    void registerShouldNotMarkMandalaSessionWhenContextIsNull() {
        // --- arrange ---
        givenSavedSession(300L, USER_ID, ActivityType.MANDALA);

        // --- act ---
        register(USER_ID, ActivityType.MANDALA, null);

        // --- assert ---
        thenMandalaSessionNotMarked();
    }

    @Test
    @DisplayName("No marca la sesión de mandala cuando el contexto está en blanco")
    void registerShouldNotMarkMandalaSessionWhenContextIsBlank() {
        // --- arrange ---
        givenSavedSession(400L, USER_ID, ActivityType.MANDALA);

        // --- act ---
        register(USER_ID, ActivityType.MANDALA, "   ");

        // --- assert ---
        thenMandalaSessionNotMarked();
    }

    // --- arrange ---

    private void givenSavedSession(Long id, Long userId, ActivityType type) {
        ActivitySession savedSession = ActivitySession.builder()
                .id(id)
                .userId(userId)
                .activityType(type)
                .createdAt(Instant.now())
                .build();
        when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(savedSession);
    }

    // --- act ---

    private RegisterActivitySessionResponse register(Long userId, ActivityType type, String contextId) {
        return registerActivitySessionUseCase.execute(
                new RegisterActivitySessionRequest(userId, type, contextId));
    }

    // --- assert ---

    private void thenSessionSaved(RegisterActivitySessionResponse result, Long expectedId, Long expectedUserId,
            ActivityType expectedType) {
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(expectedId);
        assertThat(result.userId()).isEqualTo(expectedUserId);
        assertThat(result.activityType()).isEqualTo(expectedType);
        verify(activitySessionRepository).save(any(ActivitySession.class));
    }

    private void thenMandalaSessionMarked(Long userId, String contextId) {
        verify(mandalaProgressRepository).markSessionRegistered(userId, contextId);
    }

    private void thenMandalaSessionNotMarked() {
        verify(mandalaProgressRepository, never()).markSessionRegistered(anyLong(), anyString());
    }
}
