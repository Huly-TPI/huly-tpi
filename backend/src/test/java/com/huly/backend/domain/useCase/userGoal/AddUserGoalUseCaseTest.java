package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @InjectMocks
    private AddUserGoalUseCase addUserGoalUseCase;

    @Test
    void execute_shouldSaveGoalWithPendingStatusAndReturnIt() {
        UserGoal saved = UserGoal.builder()
                .id(1L).userId(10L).title("Respirar").description("Hacer respiración")
                .activityId(2L).status(GoalStatus.PENDING).build();
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);

        UserGoal result = addUserGoalUseCase.execute(10L, "Respirar", "Hacer respiración", 2L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(GoalStatus.PENDING);
    }

    @Test
    void execute_shouldBuildGoalWithPendingStatusAndTimestamp() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        addUserGoalUseCase.execute(10L, "Respirar", null, null);

        verify(userGoalRepository).save(captor.capture());
        UserGoal captured = captor.getValue();
        assertThat(captured.getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getActivityId()).isNull();
    }

    @Test
    void execute_shouldSaveWithActivityId_whenProvided() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        addUserGoalUseCase.execute(5L, "Diario", "Escribir", 3L);

        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(3L);
        assertThat(captor.getValue().getUserId()).isEqualTo(5L);
    }
}
