package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @InjectMocks
    private DeleteUserGoalUseCase deleteUserGoalUseCase;

    private UserGoal pendingGoal(Long id) {
        return UserGoal.builder()
                .id(id).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
    }

    @Test
    void execute_shouldCancelGoal_whenItExists() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        deleteUserGoalUseCase.execute(1L);

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.CANCELLED);
        verify(userGoalRepository, never()).deleteById(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenGoalDoesNotExist() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUserGoalUseCase.execute(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userGoalRepository, never()).save(any());
        verify(userGoalRepository, never()).deleteById(any());
    }
}
