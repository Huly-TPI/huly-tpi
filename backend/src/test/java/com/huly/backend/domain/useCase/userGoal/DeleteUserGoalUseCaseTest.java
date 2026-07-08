package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.DeleteUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.DeleteUserGoalResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.userGoal.DeleteUserGoalMapper;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserGoalUseCase")
class DeleteUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    private DeleteUserGoalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserGoalUseCase(userGoalRepository, new DeleteUserGoalMapper());
    }

    @Test
    @DisplayName("Cancela la meta (soft delete) cuando existe")
    void executeShouldCancelGoalWhenItExists() {
        // --- arrange ---
        givenExistingGoal();
        givenRepositoryEchoesSavedGoal();
        // --- act ---
        DeleteUserGoalResponse result = delete(1L);
        // --- assert ---
        thenGoalWasSavedAsCancelled();
        thenResponseId(result, 1L);
        thenGoalWasNotHardDeleted();
    }

    @Test
    @DisplayName("Lanza excepción y no persiste cuando la meta no existe")
    void executeShouldThrowNotFoundWhenGoalDoesNotExist() {
        // --- arrange ---
        givenGoalNotFound();
        // --- act & assert ---
        thenDeleteThrowsNotFound();
        thenNothingWasPersisted();
    }

    // --- arrange ---

    private void givenExistingGoal() {
        UserGoal goal = UserGoal.builder()
                .id(1L).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
    }

    private void givenRepositoryEchoesSavedGoal() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenGoalNotFound() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());
    }

    // --- act ---

    private DeleteUserGoalResponse delete(Long id) {
        return useCase.execute(new DeleteUserGoalRequest(id));
    }

    // --- assert ---

    private void thenGoalWasSavedAsCancelled() {
        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.CANCELLED);
    }

    private void thenResponseId(DeleteUserGoalResponse result, Long expectedId) {
        assertThat(result.id()).isEqualTo(expectedId);
    }

    private void thenGoalWasNotHardDeleted() {
        verify(userGoalRepository, never()).deleteById(any());
    }

    private void thenDeleteThrowsNotFound() {
        assertThatThrownBy(() -> useCase.execute(new DeleteUserGoalRequest(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenNothingWasPersisted() {
        verify(userGoalRepository, never()).save(any());
        verify(userGoalRepository, never()).deleteById(any());
    }
}
