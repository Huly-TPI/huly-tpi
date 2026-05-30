package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @InjectMocks
    private DeleteUserGoalUseCase deleteUserGoalUseCase;

    @Test
    void execute_shouldDeleteGoal_whenItExists() {
        when(userGoalRepository.existsById(1L)).thenReturn(true);

        deleteUserGoalUseCase.execute(1L);

        verify(userGoalRepository).deleteById(1L);
    }

    @Test
    void execute_shouldThrowNotFoundException_whenGoalDoesNotExist() {
        when(userGoalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> deleteUserGoalUseCase.execute(99L))
                .isInstanceOf(NotFoundException.class);

        verify(userGoalRepository, never()).deleteById(any());
    }
}
