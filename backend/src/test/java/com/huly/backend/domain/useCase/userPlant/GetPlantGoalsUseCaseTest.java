package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsRequest;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.mapper.userPlant.GetPlantGoalsMapper;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPlantGoalsUseCaseTest {

    private static final Long PLANT_ID = 42L;
    private static final Long USER_ID = 10L;
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private UserGoalRepository userGoalRepository;

    private GetPlantGoalsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPlantGoalsUseCase(userGoalRepository, new GetPlantGoalsMapper());
    }

    @Test
    @DisplayName("Devuelve el id de la planta y una lista vacía cuando no hay metas completadas")
    void executeShouldReturnEmptyGoalsWhenNoneCompleted() {
        givenCompletedGoals();

        GetPlantGoalsResponse result = goals();

        thenPlantIdReturned(result);
        thenNoGoals(result);
    }

    @Test
    @DisplayName("Mapea las metas completadas de la planta a items de la respuesta")
    void executeShouldMapCompletedGoalsToItems() {
        givenCompletedGoals(
                goal(1L, "Meta uno"),
                goal(2L, "Meta dos"));

        GetPlantGoalsResponse result = goals();

        thenPlantIdReturned(result);
        thenGoalsMapped(result);
    }

    // --- arrange ---

    private void givenCompletedGoals(UserGoal... goals) {
        when(userGoalRepository.findCompletedByPlantId(PLANT_ID)).thenReturn(List.of(goals));
    }

    private UserGoal goal(Long id, String title) {
        return UserGoal.builder()
                .id(id)
                .userId(USER_ID)
                .title(title)
                .description("desc")
                .status(GoalStatus.COMPLETED)
                .createdAt(CREATED_AT)
                .activityId(99L)
                .imageUrl("http://img")
                .coinsReward(10)
                .coinsRewardWithImage(25)
                .build();
    }

    // --- act ---

    private GetPlantGoalsResponse goals() {
        return useCase.execute(new GetPlantGoalsRequest(PLANT_ID));
    }

    // --- assert ---

    private void thenPlantIdReturned(GetPlantGoalsResponse result) {
        assertThat(result.plantId()).isEqualTo(PLANT_ID);
    }

    private void thenNoGoals(GetPlantGoalsResponse result) {
        assertThat(result.goals()).isEmpty();
    }

    private void thenGoalsMapped(GetPlantGoalsResponse result) {
        assertThat(result.goals()).extracting(UserGoalItem::id).containsExactly(1L, 2L);
        assertThat(result.goals()).extracting(UserGoalItem::title).containsExactly("Meta uno", "Meta dos");
        assertThat(result.goals()).extracting(UserGoalItem::status).containsOnly("COMPLETED");
    }
}
