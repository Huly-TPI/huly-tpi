package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddUserGoalUseCase addUserGoalUseCase;

    private AppUser userWithId(Long id) {
        return AppUser.builder().id(id).email("user@test.com").build();
    }

    @Test
    void execute_shouldSaveGoalWithPendingStatusAndReturnIt() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userWithId(10L)));
        UserGoal saved = UserGoal.builder()
                .id(1L).userId(10L).title("Respirar").description("Hacer respiración")
                .activityId(2L).status(GoalStatus.PENDING).build();
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);

        UserGoal result = addUserGoalUseCase.execute("user@test.com", "Respirar", "Hacer respiración", 2L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(GoalStatus.PENDING);
    }

    @Test
    void execute_shouldBuildGoalWithPendingStatusAndTimestamp() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userWithId(10L)));
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        addUserGoalUseCase.execute("user@test.com", "Respirar", null, null);

        verify(userGoalRepository).save(captor.capture());
        UserGoal captured = captor.getValue();
        assertThat(captured.getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getActivityId()).isNull();
    }

    @Test
    void execute_shouldSaveWithActivityId_whenProvided() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userWithId(5L)));
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        addUserGoalUseCase.execute("user@test.com", "Diario", "Escribir", 3L);

        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(3L);
        assertThat(captor.getValue().getUserId()).isEqualTo(5L);
    }

    @Test
    void execute_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addUserGoalUseCase.execute("unknown@test.com", "T", null, null))
                .isInstanceOf(NotFoundException.class);
    }
}
