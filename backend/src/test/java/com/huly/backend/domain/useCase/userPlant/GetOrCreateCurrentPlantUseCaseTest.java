package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantRequest;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.mapper.userPlant.GetOrCreateCurrentPlantMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrCreateCurrentPlantUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final Long NEW_PLANT_ID = 1L;

    @Mock
    private UserPlantRepository userPlantRepository;

    private GetOrCreateCurrentPlantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOrCreateCurrentPlantUseCase(userPlantRepository, new GetOrCreateCurrentPlantMapper());
    }

    @Test
    @DisplayName("Crea la primera planta cuando el usuario no tiene ninguna")
    void resolveCurrentPlantShouldCreateFirstPlantWhenUserHasNoPlants() {
        givenNoGrowingPlant();
        givenNoPreviousPlant();
        givenSaveAssignsId(NEW_PLANT_ID);
        givenCompletedGoalsCount(NEW_PLANT_ID, 0L);

        UserPlant result = resolveCurrentPlant();

        thenFirstPlantCreated(result);
    }

    @Test
    @DisplayName("Devuelve la planta en crecimiento actual cuando el usuario ya tiene una")
    void resolveCurrentPlantShouldReturnCurrentGrowingPlantWhenUserAlreadyHasOne() {
        givenGrowingPlant(plant(2L, 2, 5, PlantStatus.GROWING));
        givenCompletedGoalsCount(2L, 3L);

        UserPlant result = resolveCurrentPlant();

        thenCurrentGrowingPlantReturned(result);
    }

    @Test
    @DisplayName("Calcula el siguiente número de planta a partir de la última del usuario")
    void resolveCurrentPlantShouldCreateNextNumberWhenUserHasPreviousPlants() {
        givenNoGrowingPlant();
        givenPreviousPlant(plant(9L, 3, 11, PlantStatus.COMPLETED));
        givenSaveAssignsId(NEW_PLANT_ID);
        givenCompletedGoalsCount(NEW_PLANT_ID, 0L);

        UserPlant result = resolveCurrentPlant();

        thenNextPlantCreated(result, 4, 14);
    }

    @Test
    @DisplayName("Usa metas requeridas aleatorias cuando el número de planta supera cinco")
    void resolveCurrentPlantShouldUseRandomRequiredGoalsWhenPlantNumberExceedsFive() {
        givenNoGrowingPlant();
        givenPreviousPlant(plant(9L, 6, 20, PlantStatus.COMPLETED));
        givenSaveAssignsId(NEW_PLANT_ID);
        givenCompletedGoalsCount(NEW_PLANT_ID, 0L);

        UserPlant result = resolveCurrentPlant();

        thenPlantCreatedWithRandomGoals(result, 7);
    }

    @Test
    @DisplayName("execute devuelve la respuesta de dominio con la planta actual")
    void executeShouldReturnDomainResponseWithCurrentPlant() {
        givenGrowingPlant(plant(2L, 2, 5, PlantStatus.GROWING));
        givenCompletedGoalsCount(2L, 3L);

        GetCurrentPlantResponse result = execute();

        thenResponseHasCurrentPlant(result);
    }

    // --- arrange ---

    private void givenGrowingPlant(UserPlant plant) {
        when(userPlantRepository.findLatestByUserIdAndStatus(USER_ID, PlantStatus.GROWING))
                .thenReturn(Optional.of(plant));
    }

    private void givenNoGrowingPlant() {
        when(userPlantRepository.findLatestByUserIdAndStatus(USER_ID, PlantStatus.GROWING))
                .thenReturn(Optional.empty());
    }

    private void givenNoPreviousPlant() {
        when(userPlantRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenPreviousPlant(UserPlant plant) {
        when(userPlantRepository.findLatestByUserId(USER_ID)).thenReturn(Optional.of(plant));
    }

    private void givenSaveAssignsId(Long id) {
        when(userPlantRepository.save(any(UserPlant.class))).thenAnswer(invocation -> {
            UserPlant plant = invocation.getArgument(0);
            plant.setId(id);
            return plant;
        });
    }

    private void givenCompletedGoalsCount(Long plantId, long count) {
        when(userPlantRepository.countCompletedGoalsByPlantId(plantId)).thenReturn(count);
    }

    private UserPlant plant(Long id, int plantNumber, int requiredGoals, PlantStatus status) {
        return UserPlant.builder()
                .id(id)
                .userId(USER_ID)
                .plantNumber(plantNumber)
                .requiredGoals(requiredGoals)
                .status(status)
                .startedAt(Instant.now())
                .build();
    }

    // --- act ---

    private UserPlant resolveCurrentPlant() {
        return useCase.resolveCurrentPlant(USER_ID);
    }

    private GetCurrentPlantResponse execute() {
        return useCase.execute(new GetCurrentPlantRequest(USER_ID));
    }

    // --- assert ---

    private void thenFirstPlantCreated(UserPlant result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlantNumber()).isEqualTo(1);
        assertThat(result.getRequiredGoals()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getCompletedGoalsCount()).isEqualTo(0L);
    }

    private void thenCurrentGrowingPlantReturned(UserPlant result) {
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getCompletedGoalsCount()).isEqualTo(3L);
    }

    private void thenNextPlantCreated(UserPlant result, int expectedNumber, int expectedRequiredGoals) {
        assertThat(result.getId()).isEqualTo(NEW_PLANT_ID);
        assertThat(result.getPlantNumber()).isEqualTo(expectedNumber);
        assertThat(result.getRequiredGoals()).isEqualTo(expectedRequiredGoals);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getCompletedGoalsCount()).isEqualTo(0L);
    }

    private void thenPlantCreatedWithRandomGoals(UserPlant result, int expectedNumber) {
        assertThat(result.getPlantNumber()).isEqualTo(expectedNumber);
        assertThat(result.getRequiredGoals()).isBetween(10, 25);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
    }

    private void thenResponseHasCurrentPlant(GetCurrentPlantResponse result) {
        assertThat(result.plant().id()).isEqualTo(2L);
        assertThat(result.plant().status()).isEqualTo("GROWING");
        assertThat(result.plant().completedGoalsCount()).isEqualTo(3L);
    }
}
