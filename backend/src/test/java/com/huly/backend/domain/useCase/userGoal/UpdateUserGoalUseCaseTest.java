package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @InjectMocks
    private UpdateUserGoalUseCase updateUserGoalUseCase;

    @Test
    void execute_shouldUpdateFieldsAndReturnSavedGoal() {
        UserGoal existing = UserGoal.builder()
                .id(1L).userId(5L).title("Viejo").description("Old").activityId(1L)
                .status(GoalStatus.PENDING).build();
        UserGoal saved = UserGoal.builder()
                .id(1L).userId(5L).title("Nuevo").description("New").activityId(2L)
                .status(GoalStatus.PENDING).build();

        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);

        UserGoal result = updateUserGoalUseCase.execute(1L, "Nuevo", "New", 2L);

        assertThat(result.getTitle()).isEqualTo("Nuevo");
        assertThat(result.getDescription()).isEqualTo("New");
        assertThat(result.getActivityId()).isEqualTo(2L);
        verify(userGoalRepository).save(existing);
    }

    @Test
    void execute_shouldSetActivityIdToNull_whenNullProvided() {
        UserGoal existing = UserGoal.builder()
                .id(1L).userId(5L).title("Viejo").activityId(1L)
                .status(GoalStatus.PENDING).build();

        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = updateUserGoalUseCase.execute(1L, "Nuevo", null, null);

        assertThat(result.getActivityId()).isNull();
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void execute_shouldThrowNotFoundException_whenGoalDoesNotExist() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUserGoalUseCase.execute(99L, "T", null, null))
                .isInstanceOf(NotFoundException.class);

        verify(userGoalRepository, never()).save(any());
    }
}
