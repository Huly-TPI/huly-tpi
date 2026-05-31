package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserGoalsByUserUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @InjectMocks
    private GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;

    private final Pageable pageable = PageRequest.of(0, 5);

    private UserGoal goal(GoalStatus status) {
        return UserGoal.builder().userId(1L).title("T").status(status).build();
    }

    @Test
    void executeCompleted_shouldDelegateWithCompletedStatus() {
        Page<UserGoal> expected = new PageImpl<>(List.of(goal(GoalStatus.COMPLETED)));
        when(userGoalRepository.findByUserIdAndStatus(1L, GoalStatus.COMPLETED, pageable)).thenReturn(expected);

        Page<UserGoal> result = getUserGoalsByUserUseCase.executeCompleted(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(GoalStatus.COMPLETED);
        verify(userGoalRepository).findByUserIdAndStatus(1L, GoalStatus.COMPLETED, pageable);
    }

    @Test
    void executePending_shouldDelegateWithPendingStatus() {
        Page<UserGoal> expected = new PageImpl<>(List.of(goal(GoalStatus.PENDING), goal(GoalStatus.PENDING)));
        when(userGoalRepository.findByUserIdAndStatus(1L, GoalStatus.PENDING, pageable)).thenReturn(expected);

        Page<UserGoal> result = getUserGoalsByUserUseCase.executePending(1L, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(userGoalRepository).findByUserIdAndStatus(1L, GoalStatus.PENDING, pageable);
    }

    @Test
    void executeCompleted_shouldReturnEmptyPage_whenNoneFound() {
        when(userGoalRepository.findByUserIdAndStatus(2L, GoalStatus.COMPLETED, pageable))
                .thenReturn(Page.empty(pageable));

        Page<UserGoal> result = getUserGoalsByUserUseCase.executeCompleted(2L, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void executePending_shouldReturnEmptyPage_whenNoneFound() {
        when(userGoalRepository.findByUserIdAndStatus(2L, GoalStatus.PENDING, pageable))
                .thenReturn(Page.empty(pageable));

        Page<UserGoal> result = getUserGoalsByUserUseCase.executePending(2L, pageable);

        assertThat(result.getContent()).isEmpty();
    }
}
