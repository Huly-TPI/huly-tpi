package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.AddUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.mapper.userGoal.AddUserGoalMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddUserGoalUseCase")
class AddUserGoalUseCaseTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    private AddUserGoalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddUserGoalUseCase(userGoalRepository, new AddUserGoalMapper());
    }

    @Test
    @DisplayName("Guarda la meta con estado PENDING y la devuelve")
    void executeShouldSaveGoalWithPendingStatusAndReturnIt() {
        // --- arrange ---
        givenRepositoryReturnsSavedGoal();
        // --- act ---
        AddUserGoalResponse result = addGoal(10L, "Respirar", "Hacer respiración", 2L);
        // --- assert ---
        thenResponseReflectsSavedGoal(result);
    }

    @Test
    @DisplayName("Construye la meta con estado PENDING, marca temporal y sin actividad")
    void executeShouldBuildGoalWithPendingStatusAndTimestamp() {
        // --- arrange ---
        givenRepositoryEchoesSavedGoal();
        // --- act ---
        addGoal(10L, "Respirar", null, null);
        // --- assert ---
        thenSavedGoalIsPendingWithoutActivity(10L);
    }

    @Test
    @DisplayName("Guarda la meta con el activityId provisto")
    void executeShouldSaveWithActivityIdWhenProvided() {
        // --- arrange ---
        givenRepositoryEchoesSavedGoal();
        // --- act ---
        addGoal(5L, "Diario", "Escribir", 3L);
        // --- assert ---
        thenSavedGoalHasActivityAndUser(3L, 5L);
    }

    // --- arrange ---

    private void givenRepositoryReturnsSavedGoal() {
        UserGoal saved = UserGoal.builder()
                .id(1L).userId(10L).title("Respirar").description("Hacer respiración")
                .activityId(2L).status(GoalStatus.PENDING).build();
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);
    }

    private void givenRepositoryEchoesSavedGoal() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- act ---

    private AddUserGoalResponse addGoal(Long userId, String title, String description, Long activityId) {
        return useCase.execute(new AddUserGoalRequest(userId, title, description, activityId));
    }

    // --- assert ---

    private void thenResponseReflectsSavedGoal(AddUserGoalResponse result) {
        assertThat(result.goal().id()).isEqualTo(1L);
        assertThat(result.goal().userId()).isEqualTo(10L);
        assertThat(result.goal().status()).isEqualTo("PENDING");
    }

    private void thenSavedGoalIsPendingWithoutActivity(Long expectedUserId) {
        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        UserGoal saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getActivityId()).isNull();
        assertThat(saved.getUserId()).isEqualTo(expectedUserId);
    }

    private void thenSavedGoalHasActivityAndUser(Long expectedActivityId, Long expectedUserId) {
        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        UserGoal saved = captor.getValue();
        assertThat(saved.getActivityId()).isEqualTo(expectedActivityId);
        assertThat(saved.getUserId()).isEqualTo(expectedUserId);
    }
}
