package com.huly.backend.domain.mapper.userPlant;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.user.UserPlant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GetOrCreateCurrentPlantMapperTest {

    private static final Long USER_ID = 9L;
    private static final Instant STARTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private final GetOrCreateCurrentPlantMapper mapper = new GetOrCreateCurrentPlantMapper();

    private UserPlant plant;

    @Test
    @DisplayName("Mapea el conteo de metas completadas cuando no es null")
    void toResponseShouldMapCompletedGoalsCountWhenPresent() {
        // --- arrange ---
        givenPlantWithCompletedGoalsCount(8L);

        // --- act ---
        GetCurrentPlantResponse result = toResponse();

        // --- assert ---
        thenItemHasCompletedGoalsCount(result, 8L);
    }

    @Test
    @DisplayName("Usa 0 como conteo de metas completadas cuando es null")
    void toResponseShouldDefaultCompletedGoalsCountToZeroWhenNull() {
        // --- arrange ---
        givenPlantWithCompletedGoalsCount(null);

        // --- act ---
        GetCurrentPlantResponse result = toResponse();

        // --- assert ---
        thenItemHasCompletedGoalsCount(result, 0L);
    }

    // --- arrange ---

    private void givenPlantWithCompletedGoalsCount(Long completedGoalsCount) {
        plant = UserPlant.builder()
                .id(1L)
                .userId(USER_ID)
                .plantNumber(2)
                .requiredGoals(3)
                .status(PlantStatus.GROWING)
                .startedAt(STARTED_AT)
                .completedAt(null)
                .completedGoalsCount(completedGoalsCount)
                .build();
    }

    // --- act ---

    private GetCurrentPlantResponse toResponse() {
        return mapper.toResponse(plant);
    }

    // --- assert ---

    private void thenItemHasCompletedGoalsCount(GetCurrentPlantResponse result, Long expected) {
        assertThat(result.plant().completedGoalsCount()).isEqualTo(expected);
    }
}
