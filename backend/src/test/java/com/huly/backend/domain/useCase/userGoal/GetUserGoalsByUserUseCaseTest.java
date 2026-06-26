package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.mapper.userGoal.GetUserGoalsMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserGoalsByUserUseCaseTest {

    @Mock private UserGoalRepository userGoalRepository;

    private GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;

    @BeforeEach
    void setUp() {
        getUserGoalsByUserUseCase = new GetUserGoalsByUserUseCase(userGoalRepository, new GetUserGoalsMapper());
    }

    private UserGoal goal(GoalStatus status) {
        return UserGoal.builder().userId(1L).title("T").status(status).build();
    }

    @Test
    void execute_shouldReturnCompletedAndPendingPages() {
        Page<UserGoal> completed = new PageImpl<>(List.of(goal(GoalStatus.COMPLETED)),
                PageRequest.of(0, 5), 1);
        Page<UserGoal> pending = new PageImpl<>(
                List.of(goal(GoalStatus.PENDING), goal(GoalStatus.PENDING)),
                PageRequest.of(0, 5), 2);
        when(userGoalRepository.findByUserIdAndStatus(eq(1L), eq(GoalStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(completed);
        when(userGoalRepository.findByUserIdAndStatus(eq(1L), eq(GoalStatus.PENDING), any(Pageable.class)))
                .thenReturn(pending);

        GetUserGoalsResponse result = getUserGoalsByUserUseCase.execute(new GetUserGoalsRequest(1L, 0, 5));

        assertThat(result.completados().content()).hasSize(1);
        assertThat(result.completados().content().get(0).status()).isEqualTo("COMPLETED");
        assertThat(result.completados().totalElements()).isEqualTo(1);
        assertThat(result.completados().pageSize()).isEqualTo(5);
        assertThat(result.pendientes().content()).hasSize(2);
        assertThat(result.pendientes().totalElements()).isEqualTo(2);
        verify(userGoalRepository).findByUserIdAndStatus(eq(1L), eq(GoalStatus.COMPLETED), any(Pageable.class));
        verify(userGoalRepository).findByUserIdAndStatus(eq(1L), eq(GoalStatus.PENDING), any(Pageable.class));
    }

    @Test
    void execute_shouldReturnEmptyPages_whenNoneFound() {
        when(userGoalRepository.findByUserIdAndStatus(eq(2L), eq(GoalStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 5)));
        when(userGoalRepository.findByUserIdAndStatus(eq(2L), eq(GoalStatus.PENDING), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 5)));

        GetUserGoalsResponse result = getUserGoalsByUserUseCase.execute(new GetUserGoalsRequest(2L, 0, 5));

        assertThat(result.completados().content()).isEmpty();
        assertThat(result.completados().totalElements()).isZero();
        assertThat(result.pendientes().content()).isEmpty();
    }

    @Test
    void execute_shouldUsePageAndSizeFromRequest() {
        when(userGoalRepository.findByUserIdAndStatus(eq(1L), eq(GoalStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(1, 3)));
        when(userGoalRepository.findByUserIdAndStatus(eq(1L), eq(GoalStatus.PENDING), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(1, 3)));

        GetUserGoalsResponse result = getUserGoalsByUserUseCase.execute(new GetUserGoalsRequest(1L, 1, 3));

        assertThat(result.completados().pageNumber()).isEqualTo(1);
        assertThat(result.completados().pageSize()).isEqualTo(3);
    }
}
