package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.model.user.UserEmotionalState;
import com.huly.backend.domain.repository.user.UserEmotionalStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveUserEmotionalStateUseCaseTest {
    
    @Mock
    private UserEmotionalStateRepository repository;

    @InjectMocks
    private SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase;

    @Test
    void execute_shouldSaveAndReturnState() {
        UserEmotionalState inputState = UserEmotionalState.builder()
               .id(1L)
                .userId(10L)
                .valence(0.5)
                .arousal(-0.3)
                .dominance(0.2)
                .intensity(0.8)
                .source("chatbot")
                .timestamp(Instant.now())
                .build();
        when(repository.save(any(UserEmotionalState.class))).thenReturn(inputState);

              UserEmotionalState result = saveUserEmotionalStateUseCase.execute(
                10L, 0.5, -0.3, 0.2, 0.8, "chatbot");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getSource()).isEqualTo("chatbot");
        verify(repository).save(any(UserEmotionalState.class));
    }

    @Test
    void execute_shouldSetTimestampAutomatically() { 
       when(repository.save(any(UserEmotionalState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEmotionalState result = saveUserEmotionalStateUseCase.execute(
                10L, 0.5, -0.3, 0.2, 0.8, "diario");

                assertThat(result.getTimestamp()).isNotNull();
    }
    
}
