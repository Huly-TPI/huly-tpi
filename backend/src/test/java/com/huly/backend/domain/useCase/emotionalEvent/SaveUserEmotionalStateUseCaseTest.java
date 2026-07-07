package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.domain.mapper.emotionalEvent.SaveUserEmotionalStateMapper;
import com.huly.backend.domain.model.user.UserEmotionalState;
import com.huly.backend.domain.repository.user.UserEmotionalStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveUserEmotionalStateUseCaseTest {

    private static final Long PERSISTED_ID = 1L;
    private static final Long USER_ID = 10L;

    @Mock
    private UserEmotionalStateRepository repository;

    private SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase;
    private SaveUserEmotionalStateRequest request;

    @BeforeEach
    void setUp() {
        saveUserEmotionalStateUseCase = new SaveUserEmotionalStateUseCase(
                repository, new SaveUserEmotionalStateMapper());
    }

    @Test
    @DisplayName("Guarda el estado emocional y devuelve la respuesta mapeada")
    void executeShouldSaveAndReturnState() {
        // --- arrange ---
        givenStateRequest("chatbot");
        givenRepositoryReturnsPersistedState();

        // --- act ---
        SaveUserEmotionalStateResponse result = save();

        // --- assert ---
        thenPersistedStateIsReturned(result);
    }

    @Test
    @DisplayName("Asigna el timestamp automáticamente al guardar")
    void executeShouldSetTimestampAutomatically() {
        // --- arrange ---
        givenStateRequest("diario");
        givenRepositoryReturnsSavedStateAsIs();

        // --- act ---
        SaveUserEmotionalStateResponse result = save();

        // --- assert ---
        thenTimestampIsSet(result);
    }

    // --- arrange ---

    private void givenStateRequest(String source) {
        request = new SaveUserEmotionalStateRequest(USER_ID, 0.5, -0.3, 0.2, 0.8, source);
    }

    private void givenRepositoryReturnsPersistedState() {
        when(repository.save(any(UserEmotionalState.class))).thenReturn(persistedState());
    }

    private void givenRepositoryReturnsSavedStateAsIs() {
        when(repository.save(any(UserEmotionalState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private UserEmotionalState persistedState() {
        return UserEmotionalState.builder()
                .id(PERSISTED_ID)
                .userId(USER_ID)
                .valence(0.5)
                .arousal(-0.3)
                .dominance(0.2)
                .intensity(0.8)
                .source("chatbot")
                .timestamp(Instant.now())
                .build();
    }

    // --- act ---

    private SaveUserEmotionalStateResponse save() {
        return saveUserEmotionalStateUseCase.execute(request);
    }

    // --- assert ---

    private void thenPersistedStateIsReturned(SaveUserEmotionalStateResponse result) {
        assertThat(result.id()).isEqualTo(PERSISTED_ID);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.source()).isEqualTo("chatbot");
        verify(repository).save(any(UserEmotionalState.class));
    }

    private void thenTimestampIsSet(SaveUserEmotionalStateResponse result) {
        assertThat(result.timestamp()).isNotNull();
    }
}
