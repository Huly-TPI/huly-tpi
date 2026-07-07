package com.huly.backend.domain.mapper.userPlant;

import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.user.UserPlant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetUserPlantsMapperTest {

    private static final Long USER_ID = 9L;
    private static final Instant STARTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private final GetUserPlantsMapper mapper = new GetUserPlantsMapper();

    private List<UserPlant> plants;

    @Test
    @DisplayName("Mapea el conteo de metas completadas cuando no es null")
    void toResponseShouldMapCompletedGoalsCountWhenPresent() {
        // --- arrange ---
        givenPlantWithCompletedGoalsCount(5L);

        // --- act ---
        GetUserPlantsResponse result = toResponse();

        // --- assert ---
        thenFirstItemHasCompletedGoalsCount(result, 5L);
    }

    @Test
    @DisplayName("Usa 0 como conteo de metas completadas cuando es null")
    void toResponseShouldDefaultCompletedGoalsCountToZeroWhenNull() {
        // --- arrange ---
        givenPlantWithCompletedGoalsCount(null);

        // --- act ---
        GetUserPlantsResponse result = toResponse();

        // --- assert ---
        thenFirstItemHasCompletedGoalsCount(result, 0L);
    }

    // --- arrange ---

    private void givenPlantWithCompletedGoalsCount(Long completedGoalsCount) {
        plants = List.of(UserPlant.builder()
                .id(1L)
                .userId(USER_ID)
                .plantNumber(2)
                .requiredGoals(3)
                .status(PlantStatus.GROWING)
                .startedAt(STARTED_AT)
                .completedAt(null)
                .completedGoalsCount(completedGoalsCount)
                .build());
    }

    // --- act ---

    private GetUserPlantsResponse toResponse() {
        return mapper.toResponse(plants);
    }

    // --- assert ---

    private void thenFirstItemHasCompletedGoalsCount(GetUserPlantsResponse result, Long expected) {
        assertThat(result.plants()).hasSize(1);
        assertThat(result.plants().get(0).completedGoalsCount()).isEqualTo(expected);
    }
}
