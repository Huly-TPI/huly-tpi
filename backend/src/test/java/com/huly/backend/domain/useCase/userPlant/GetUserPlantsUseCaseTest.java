package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetUserPlantsRequest;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.mapper.userPlant.GetUserPlantsMapper;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.repository.UserPlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserPlantsUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final Instant STARTED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private UserPlantRepository userPlantRepository;

    private GetUserPlantsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserPlantsUseCase(userPlantRepository, new GetUserPlantsMapper());
    }

    @Test
    @DisplayName("Devuelve una lista vacía y no cuenta metas cuando el usuario no tiene plantas")
    void executeShouldReturnEmptyWhenUserHasNoPlants() {
        givenPlants();

        GetUserPlantsResponse result = list();

        thenEmpty(result);
        thenGoalsWereNotCounted();
    }

    @Test
    @DisplayName("Mapea cada planta con su conteo de metas completadas")
    void executeShouldMapPlantsWithCompletedGoalsCount() {
        givenPlants(
                plant(1L, 1, PlantStatus.COMPLETED),
                plant(2L, 2, PlantStatus.GROWING));
        givenCompletedGoalsCount(1L, 3L);
        givenCompletedGoalsCount(2L, 0L);

        GetUserPlantsResponse result = list();

        thenPlantsMappedWithCounts(result);
    }

    // --- arrange ---

    private void givenPlants(UserPlant... plants) {
        when(userPlantRepository.findAllByUserIdOrderByPlantNumber(USER_ID)).thenReturn(List.of(plants));
    }

    private void givenCompletedGoalsCount(Long plantId, long count) {
        when(userPlantRepository.countCompletedGoalsByPlantId(plantId)).thenReturn(count);
    }

    private UserPlant plant(Long id, int plantNumber, PlantStatus status) {
        return UserPlant.builder()
                .id(id)
                .userId(USER_ID)
                .plantNumber(plantNumber)
                .requiredGoals(5)
                .status(status)
                .startedAt(STARTED_AT)
                .build();
    }

    // --- act ---

    private GetUserPlantsResponse list() {
        return useCase.execute(new GetUserPlantsRequest(USER_ID));
    }

    // --- assert ---

    private void thenEmpty(GetUserPlantsResponse result) {
        assertThat(result.plants()).isEmpty();
    }

    private void thenGoalsWereNotCounted() {
        verify(userPlantRepository, never()).countCompletedGoalsByPlantId(anyLong());
    }

    private void thenPlantsMappedWithCounts(GetUserPlantsResponse result) {
        assertThat(result.plants()).extracting(UserPlantItem::id).containsExactly(1L, 2L);
        assertThat(result.plants()).extracting(UserPlantItem::status).containsExactly("COMPLETED", "GROWING");
        assertThat(result.plants()).extracting(UserPlantItem::completedGoalsCount).containsExactly(3L, 0L);
    }
}
