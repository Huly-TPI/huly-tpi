package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantRequest;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.mapper.userPlant.GetOrCreateCurrentPlantMapper;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.repository.UserPlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateCurrentPlantUseCaseTest {

    private static final Long USER_ID = 10L;

    @Mock
    private UserPlantRepository userPlantRepository;

    private GetOrCreateCurrentPlantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOrCreateCurrentPlantUseCase(userPlantRepository, new GetOrCreateCurrentPlantMapper());
    }

    @Test
    void resolveCurrentPlant_shouldCreateFirstPlant_whenUserHasNoPlants() {
        when(userPlantRepository.findLatestByUserIdAndStatus(USER_ID, PlantStatus.GROWING)).thenReturn(Optional.empty());
        when(userPlantRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userPlantRepository.save(any(UserPlant.class))).thenAnswer(invocation -> {
            UserPlant plant = invocation.getArgument(0);
            plant.setId(1L);
            return plant;
        });
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(0L);

        UserPlant result = useCase.resolveCurrentPlant(USER_ID);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlantNumber()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getCompletedGoalsCount()).isEqualTo(0L);
    }

    @Test
    void resolveCurrentPlant_shouldReturnCurrentGrowingPlant_whenUserAlreadyHasOne() {
        UserPlant currentGrowingPlant = plant(2L, 2, PlantStatus.GROWING);

        when(userPlantRepository.findLatestByUserIdAndStatus(USER_ID, PlantStatus.GROWING))
                .thenReturn(Optional.of(currentGrowingPlant));
        when(userPlantRepository.countCompletedGoalsByPlantId(2L)).thenReturn(3L);

        UserPlant result = useCase.resolveCurrentPlant(USER_ID);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getCompletedGoalsCount()).isEqualTo(3L);
    }

    @Test
    void execute_shouldReturnDomainResponse_withCurrentPlant() {
        UserPlant currentGrowingPlant = plant(2L, 2, PlantStatus.GROWING);

        when(userPlantRepository.findLatestByUserIdAndStatus(USER_ID, PlantStatus.GROWING))
                .thenReturn(Optional.of(currentGrowingPlant));
        when(userPlantRepository.countCompletedGoalsByPlantId(2L)).thenReturn(3L);

        GetCurrentPlantResponse result = useCase.execute(new GetCurrentPlantRequest(USER_ID));

        assertThat(result.plant().id()).isEqualTo(2L);
        assertThat(result.plant().status()).isEqualTo("GROWING");
        assertThat(result.plant().completedGoalsCount()).isEqualTo(3L);
    }

    private UserPlant plant(Long id, int plantNumber, PlantStatus status) {
        return UserPlant.builder()
                .id(id)
                .userId(USER_ID)
                .plantNumber(plantNumber)
                .requiredGoals(5)
                .status(status)
                .startedAt(Instant.now())
                .build();
    }
}
