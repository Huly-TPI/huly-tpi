package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.UpdateUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.userGoal.UpdateUserGoalMapper;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserGoalUseCase")
class UpdateUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    private UpdateUserGoalUseCase useCase;

    private UserGoal existingGoal;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserGoalUseCase(userGoalRepository, new UpdateUserGoalMapper());
    }

    @Test
    @DisplayName("Actualiza los campos y devuelve la meta guardada")
    void executeShouldUpdateFieldsAndReturnSavedGoal() {
        // --- arrange ---
        givenExistingGoal();
        givenRepositoryReturnsUpdatedGoal();
        // --- act ---
        UpdateUserGoalResponse result = update(1L, "Nuevo", "New", 2L);
        // --- assert ---
        thenResponseHasUpdatedFields(result);
        thenExistingGoalWasSaved();
    }

    @Test
    @DisplayName("Deja el activityId y la descripción en null cuando llegan nulos")
    void executeShouldSetOptionalFieldsToNullWhenNullProvided() {
        // --- arrange ---
        givenExistingGoal();
        givenRepositoryEchoesSavedGoal();
        // --- act ---
        UpdateUserGoalResponse result = update(1L, "Nuevo", null, null);
        // --- assert ---
        thenResponseHasClearedOptionalFields(result);
    }

    @Test
    @DisplayName("Lanza excepción y no guarda cuando la meta no existe")
    void executeShouldThrowNotFoundWhenGoalDoesNotExist() {
        // --- arrange ---
        givenGoalNotFound();
        // --- act & assert ---
        thenUpdateThrowsNotFound();
        thenGoalWasNotSaved();
    }

    // --- arrange ---

    private void givenExistingGoal() {
        existingGoal = UserGoal.builder()
                .id(1L).userId(5L).title("Viejo").description("Old").activityId(1L)
                .status(GoalStatus.PENDING).build();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(existingGoal));
    }

    private void givenRepositoryReturnsUpdatedGoal() {
        UserGoal saved = UserGoal.builder()
                .id(1L).userId(5L).title("Nuevo").description("New").activityId(2L)
                .status(GoalStatus.PENDING).build();
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);
    }

    private void givenRepositoryEchoesSavedGoal() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenGoalNotFound() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());
    }

    // --- act ---

    private UpdateUserGoalResponse update(Long id, String title, String description, Long activityId) {
        return useCase.execute(new UpdateUserGoalRequest(id, title, description, activityId));
    }

    // --- assert ---

    private void thenResponseHasUpdatedFields(UpdateUserGoalResponse result) {
        assertThat(result.goal().title()).isEqualTo("Nuevo");
        assertThat(result.goal().description()).isEqualTo("New");
        assertThat(result.goal().activityId()).isEqualTo(2L);
    }

    private void thenExistingGoalWasSaved() {
        verify(userGoalRepository).save(existingGoal);
    }

    private void thenResponseHasClearedOptionalFields(UpdateUserGoalResponse result) {
        assertThat(result.goal().activityId()).isNull();
        assertThat(result.goal().description()).isNull();
    }

    private void thenUpdateThrowsNotFound() {
        assertThatThrownBy(() -> useCase.execute(new UpdateUserGoalRequest(99L, "T", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenGoalWasNotSaved() {
        verify(userGoalRepository, never()).save(any());
    }
}
