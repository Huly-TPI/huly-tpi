package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.mapper.userGoal.GetUserGoalsMapper;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("GetUserGoalsByUserUseCase")
class GetUserGoalsByUserUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    private GetUserGoalsByUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserGoalsByUserUseCase(userGoalRepository, new GetUserGoalsMapper());
    }

    @Test
    @DisplayName("Devuelve las páginas de metas completadas y pendientes")
    void executeShouldReturnCompletedAndPendingPages() {
        // --- arrange ---
        givenCompletedPageWithOneGoal(1L);
        givenPendingPageWithTwoGoals(1L);
        // --- act ---
        GetUserGoalsResponse result = getGoals(1L, 0, 5);
        // --- assert ---
        thenCompletedHasOneCompletedGoal(result);
        thenPendingHasTwoGoals(result);
        thenBothStatusesWereQueried(1L);
    }

    @Test
    @DisplayName("Devuelve páginas vacías cuando no hay metas")
    void executeShouldReturnEmptyPagesWhenNoneFound() {
        // --- arrange ---
        givenEmptyCompletedPage(2L, 0, 5);
        givenEmptyPendingPage(2L, 0, 5);
        // --- act ---
        GetUserGoalsResponse result = getGoals(2L, 0, 5);
        // --- assert ---
        thenBothPagesAreEmpty(result);
    }

    @Test
    @DisplayName("Refleja el número de página y el tamaño de página consultados")
    void executeShouldUsePageAndSizeFromRequest() {
        // --- arrange ---
        givenEmptyCompletedPage(1L, 1, 3);
        givenEmptyPendingPage(1L, 1, 3);
        // --- act ---
        GetUserGoalsResponse result = getGoals(1L, 1, 3);
        // --- assert ---
        thenCompletedPageIndexAndSize(result, 1, 3);
    }

    // --- arrange ---

    private void givenCompletedPageWithOneGoal(Long userId) {
        Page<UserGoal> page = new PageImpl<>(
                List.of(goal(GoalStatus.COMPLETED)), PageRequest.of(0, 5), 1);
        when(userGoalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(page);
    }

    private void givenPendingPageWithTwoGoals(Long userId) {
        Page<UserGoal> page = new PageImpl<>(
                List.of(goal(GoalStatus.PENDING), goal(GoalStatus.PENDING)), PageRequest.of(0, 5), 2);
        when(userGoalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.PENDING), any(Pageable.class)))
                .thenReturn(page);
    }

    private void givenEmptyCompletedPage(Long userId, int page, int size) {
        when(userGoalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(page, size)));
    }

    private void givenEmptyPendingPage(Long userId, int page, int size) {
        when(userGoalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.PENDING), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(page, size)));
    }

    private UserGoal goal(GoalStatus status) {
        return UserGoal.builder().userId(1L).title("T").status(status).build();
    }

    // --- act ---

    private GetUserGoalsResponse getGoals(Long userId, int page, int size) {
        return useCase.execute(new GetUserGoalsRequest(userId, page, size));
    }

    // --- assert ---

    private void thenCompletedHasOneCompletedGoal(GetUserGoalsResponse result) {
        assertThat(result.completados().content()).hasSize(1);
        assertThat(result.completados().content().get(0).status()).isEqualTo("COMPLETED");
        assertThat(result.completados().totalElements()).isEqualTo(1);
        assertThat(result.completados().pageSize()).isEqualTo(5);
    }

    private void thenPendingHasTwoGoals(GetUserGoalsResponse result) {
        assertThat(result.pendientes().content()).hasSize(2);
        assertThat(result.pendientes().totalElements()).isEqualTo(2);
    }

    private void thenBothStatusesWereQueried(Long userId) {
        verify(userGoalRepository).findByUserIdAndStatus(eq(userId), eq(GoalStatus.COMPLETED), any(Pageable.class));
        verify(userGoalRepository).findByUserIdAndStatus(eq(userId), eq(GoalStatus.PENDING), any(Pageable.class));
    }

    private void thenBothPagesAreEmpty(GetUserGoalsResponse result) {
        assertThat(result.completados().content()).isEmpty();
        assertThat(result.completados().totalElements()).isZero();
        assertThat(result.pendientes().content()).isEmpty();
    }

    private void thenCompletedPageIndexAndSize(GetUserGoalsResponse result, int page, int size) {
        assertThat(result.completados().pageNumber()).isEqualTo(page);
        assertThat(result.completados().pageSize()).isEqualTo(size);
    }
}
