package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
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
class CompleteUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @Mock
    private UserPlantRepository userPlantRepository;

    @Mock
    private GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;

    @InjectMocks
    private CompleteUserGoalUseCase completeUserGoalUseCase;

    private UserGoal pendingGoal(Long id) {
        return UserGoal.builder()
                .id(id).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
    }

    private UserPlant activePlant() {
        return UserPlant.builder()
                .id(1L).userId(10L).plantNumber(1).requiredGoals(5)
                .status(PlantStatus.GROWING).startedAt(Instant.now()).build();
    }

    @Test
    void execute_shouldSetStatusToCompleted_whenGoalExists() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L);

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(result.goal().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void execute_shouldNotModifyOtherFields_whenCompleting() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L);

        assertThat(result.goal().getId()).isEqualTo(1L);
        assertThat(result.goal().getUserId()).isEqualTo(10L);
        assertThat(result.goal().getTitle()).isEqualTo("Meta");
    }

    @Test
    void execute_shouldThrowNotFoundException_whenGoalDoesNotExist() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userGoalRepository, never()).save(any());
    }
}
