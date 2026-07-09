package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.mapper.userGoal.AcceptChallengeMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcceptChallengeUseCase")
class AcceptChallengeUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final Long GOAL_ID = 1L;
    private static final Long ACTIVITY_ID = 2L;
    private static final String TITLE = "Reto de respiración";
    private static final String DESCRIPTION = "Respirar 5 minutos";

    @Mock
    private UserGoalRepository userGoalRepository;

    private AcceptChallengeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AcceptChallengeUseCase(userGoalRepository, new AcceptChallengeMapper());
    }

    @Test
    @DisplayName("Acepta el reto y devuelve la meta creada")
    void executeShouldAcceptChallengeAndReturnCreatedGoal() {
        // --- arrange ---
        givenRepositoryReturnsSavedGoal();
        // --- act ---
        AcceptChallengeResponse result = acceptChallenge();
        // --- assert ---
        thenResponseReflectsSavedGoal(result);
    }

    @Test
    @DisplayName("Construye la meta con estado PENDING y marca temporal al aceptar el reto")
    void executeShouldBuildGoalWithPendingStatusAndTimestamp() {
        // --- arrange ---
        givenRepositoryEchoesSavedGoal();
        // --- act ---
        acceptChallenge();
        // --- assert ---
        thenSavedGoalIsPendingWithChallengeData();
    }

    // --- arrange ---

    private void givenRepositoryReturnsSavedGoal() {
        UserGoal saved = UserGoal.builder()
                .id(GOAL_ID).userId(USER_ID).title(TITLE).description(DESCRIPTION)
                .activityId(ACTIVITY_ID).status(GoalStatus.PENDING).createdAt(Instant.now())
                .build();
        when(userGoalRepository.save(any(UserGoal.class))).thenReturn(saved);
    }

    private void givenRepositoryEchoesSavedGoal() {
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- act ---

    private AcceptChallengeResponse acceptChallenge() {
        return useCase.execute(new AcceptChallengeRequest(USER_ID, TITLE, DESCRIPTION, ACTIVITY_ID));
    }

    // --- assert ---

    private void thenResponseReflectsSavedGoal(AcceptChallengeResponse result) {
        assertThat(result.goal().id()).isEqualTo(GOAL_ID);
        assertThat(result.goal().userId()).isEqualTo(USER_ID);
        assertThat(result.goal().title()).isEqualTo(TITLE);
        assertThat(result.goal().status()).isEqualTo("PENDING");
    }

    private void thenSavedGoalIsPendingWithChallengeData() {
        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        UserGoal saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(TITLE);
        assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(saved.getActivityId()).isEqualTo(ACTIVITY_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
    }
}
