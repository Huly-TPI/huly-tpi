package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.CompleteUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.exception.InvalidGoalImageException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.userGoal.CompleteUserGoalMapper;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.goals.ImageValidationResult;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.port.ImageValidationPort;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteUserGoalUseCase")
class CompleteUserGoalUseCaseTest {

    private static final Long GOAL_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long PLANT_ID = 1L;
    private static final Long MISSING_ID = 99L;
    private static final String IMAGE_URL = "/api/user-goals/images/photo.jpg";

    @Mock
    private UserGoalRepository userGoalRepository;

    @Mock
    private UserPlantRepository userPlantRepository;

    @Mock
    private GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;

    @Mock
    private CoinService coinService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ImageValidationPort imageValidationPort;

    private CompleteUserGoalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteUserGoalUseCase(
                userGoalRepository, userPlantRepository, getOrCreateCurrentPlantUseCase,
                coinService, imageStorageService, imageValidationPort, new CompleteUserGoalMapper());
    }

    @Test
    @DisplayName("Marca la meta como COMPLETED cuando existe")
    void executeShouldSetStatusToCompletedWhenGoalExists() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        // --- act ---
        CompleteUserGoalResponse result = complete();
        // --- assert ---
        thenGoalWasSavedAsCompleted();
        thenResponseGoalIsCompleted(result);
    }

    @Test
    @DisplayName("Conserva id, usuario y título al completar la meta")
    void executeShouldNotModifyIdentityFieldsWhenCompleting() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        // --- act ---
        CompleteUserGoalResponse result = complete();
        // --- assert ---
        thenResponseGoalIdentityPreserved(result);
    }

    @Test
    @DisplayName("Lanza excepción y no guarda cuando la meta no existe")
    void executeShouldThrowNotFoundWhenGoalDoesNotExist() {
        // --- arrange ---
        givenMissingGoal();
        // --- act & assert ---
        thenCompleteMissingThrowsNotFound();
        thenGoalWasNotSaved();
    }

    @Test
    @DisplayName("Acredita las monedas base cuando no se envía imagen")
    void executeShouldCreditBaseCoinsWhenNoImageProvided() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        // --- act ---
        complete();
        // --- assert ---
        thenCoinsCredited(10);
    }

    @Test
    @DisplayName("Acredita las monedas con bonus cuando se envía imagen")
    void executeShouldCreditBonusCoinsWhenImageProvided() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        MultipartFile image = givenValidImage(IMAGE_URL);
        // --- act ---
        completeWith(image);
        // --- assert ---
        thenCoinsCredited(25);
    }

    @Test
    @DisplayName("Guarda la URL de la imagen cuando se envía imagen")
    void executeShouldSetImageUrlWhenImageProvided() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        MultipartFile image = givenValidImage(IMAGE_URL);
        // --- act ---
        CompleteUserGoalResponse result = completeWith(image);
        // --- assert ---
        thenResponseImageUrlIs(result, IMAGE_URL);
    }

    @Test
    @DisplayName("Acredita las monedas base cuando la imagen enviada está vacía")
    void executeShouldCreditBaseCoinsWhenImageIsEmpty() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        MultipartFile image = givenEmptyImage();
        // --- act ---
        CompleteUserGoalResponse result = completeWith(image);
        // --- assert ---
        thenCoinsCredited(10);
        thenResponseHasNoImageUrl(result);
    }

    @Test
    @DisplayName("Devuelve temprano sin guardar ni acreditar cuando la meta ya está completada")
    void executeShouldReturnEarlyWithoutSavingWhenGoalAlreadyCompleted() {
        // --- arrange ---
        givenExistingCompletedGoal();
        givenResolvedPlant();
        givenCompletedGoalsCount(1L);
        // --- act ---
        CompleteUserGoalResponse result = complete();
        // --- assert ---
        thenNothingWasSavedNorCredited();
        thenResponseGoalIsCompleted(result);
    }

    @Test
    @DisplayName("Usa las monedas configuradas de la meta cuando coinsReward está definido")
    void executeShouldUseCustomCoinsRewardWhenCoinsRewardIsSet() {
        // --- arrange ---
        givenExistingGoalWithCoinsReward(15);
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        // --- act ---
        complete();
        // --- assert ---
        thenCoinsCredited(15);
    }

    @Test
    @DisplayName("Usa las monedas con imagen configuradas cuando coinsRewardWithImage está definido")
    void executeShouldUseCustomCoinsRewardWithImageWhenSet() {
        // --- arrange ---
        givenExistingGoalWithCoinsRewardWithImage(40);
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(1L);
        MultipartFile image = givenValidImage(IMAGE_URL);
        // --- act ---
        completeWith(image);
        // --- assert ---
        thenCoinsCredited(40);
    }

    @Test
    @DisplayName("Persiste la planta completada antes de crear la siguiente cuando se dispara la cosecha")
    void executeShouldFlushCompletedPlantBeforeCreatingNextPlantWhenHarvestIsTriggered() {
        // --- arrange ---
        givenExistingPendingGoal();
        givenResolvedPlant();
        givenGoalSaveEchoes();
        givenCompletedGoalsCount(5L);
        givenNextPlantCreatedOnSave();
        // --- act ---
        CompleteUserGoalResponse result = complete();
        // --- assert ---
        thenCompletedPlantWasFlushedBeforeNextPlantCreated();
        thenHarvestResult(result);
    }

    @Test
    @DisplayName("Lanza InvalidGoalImage cuando la imagen supera el tamaño máximo")
    void executeShouldThrowInvalidGoalImageWhenFileSizeExceedsLimit() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenOversizedImage();
        // --- act & assert ---
        thenCompleteWithImageThrowsInvalidImage(image, "La imagen no puede superar los 5 MB.");
        thenValidationWasNotAttempted();
        thenGoalWasNotSaved();
    }

    @Test
    @DisplayName("Lanza InvalidGoalImage cuando el tipo MIME no está permitido")
    void executeShouldThrowInvalidGoalImageWhenMimeTypeIsInvalid() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenImageWithContentType("video/mp4");
        // --- act & assert ---
        thenCompleteWithImageThrowsInvalidImage(image);
        thenGoalWasNotSaved();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Lanza InvalidGoalImage cuando el tipo MIME es nulo")
    void executeShouldThrowInvalidGoalImageWhenMimeTypeIsNull() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenImageWithContentType(null);
        // --- act & assert ---
        thenCompleteWithImageThrowsInvalidImage(image);
        thenGoalWasNotSaved();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Lanza InvalidGoalImage cuando la validación por IA rechaza la imagen")
    void executeShouldThrowInvalidGoalImageWhenAiRejectsImage() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenImageRejectedByAi();
        // --- act & assert ---
        thenCompleteWithImageThrowsInvalidImage(image, "La imagen no tiene relación con el reto");
        thenImageWasNotStored();
        thenGoalWasNotSaved();
    }

    @Test
    @DisplayName("Propaga ImageValidationUnavailable cuando el puerto de validación falla")
    void executeShouldThrowImageValidationUnavailableWhenPortThrows() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenImageWhereValidationServiceFails();
        // --- act & assert ---
        thenCompleteWithImageThrowsValidationUnavailable(image);
        thenGoalWasNotSaved();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Lanza ImageValidationUnavailable cuando no se puede leer la imagen")
    void executeShouldThrowImageValidationUnavailableWhenGetBytesThrows() {
        // --- arrange ---
        givenExistingPendingGoal();
        MultipartFile image = givenUnreadableImage();
        // --- act & assert ---
        thenCompleteWithImageThrowsValidationUnavailable(image);
        thenValidationWasNotAttempted();
        thenGoalWasNotSaved();
    }

    // --- arrange ---

    private void givenExistingPendingGoal() {
        when(userGoalRepository.findById(GOAL_ID)).thenReturn(Optional.of(pendingGoal()));
    }

    private void givenExistingCompletedGoal() {
        UserGoal completed = UserGoal.builder()
                .id(GOAL_ID).userId(USER_ID).title("Meta").status(GoalStatus.COMPLETED)
                .createdAt(Instant.now()).build();
        when(userGoalRepository.findById(GOAL_ID)).thenReturn(Optional.of(completed));
    }

    private void givenExistingGoalWithCoinsReward(int coins) {
        UserGoal goal = UserGoal.builder()
                .id(GOAL_ID).userId(USER_ID).title("Meta").status(GoalStatus.PENDING)
                .coinsReward(coins).createdAt(Instant.now()).build();
        when(userGoalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
    }

    private void givenExistingGoalWithCoinsRewardWithImage(int coins) {
        UserGoal goal = UserGoal.builder()
                .id(GOAL_ID).userId(USER_ID).title("Meta").status(GoalStatus.PENDING)
                .coinsRewardWithImage(coins).createdAt(Instant.now()).build();
        when(userGoalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
    }

    private void givenMissingGoal() {
        when(userGoalRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
    }

    private void givenResolvedPlant() {
        when(getOrCreateCurrentPlantUseCase.resolveCurrentPlant(USER_ID)).thenReturn(activePlant());
    }

    private void givenGoalSaveEchoes() {
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenCompletedGoalsCount(long count) {
        when(userPlantRepository.countCompletedGoalsByPlantId(PLANT_ID)).thenReturn(count);
    }

    private void givenNextPlantCreatedOnSave() {
        UserPlant nextPlant = UserPlant.builder()
                .id(2L).userId(USER_ID).plantNumber(2).requiredGoals(8)
                .status(PlantStatus.GROWING).startedAt(Instant.now()).build();
        when(userPlantRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.save(argThat((UserPlant plant) -> plant.getId() == null))).thenReturn(nextPlant);
    }

    private MultipartFile givenValidImage(String returnedUrl) {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        stubReadableBytes(image);
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenReturn(new ImageValidationResult(true, "La imagen es válida"));
        when(imageStorageService.save(image)).thenReturn(returnedUrl);
        return image;
    }

    private MultipartFile givenEmptyImage() {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);
        return image;
    }

    private MultipartFile givenOversizedImage() {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getSize()).thenReturn(6L * 1024 * 1024);
        return image;
    }

    private MultipartFile givenImageWithContentType(String contentType) {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn(contentType);
        return image;
    }

    private MultipartFile givenImageRejectedByAi() {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        stubReadableBytes(image);
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenReturn(new ImageValidationResult(false, "La imagen no tiene relación con el reto"));
        return image;
    }

    private MultipartFile givenImageWhereValidationServiceFails() {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        stubReadableBytes(image);
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenThrow(new ImageValidationUnavailableException("Servicio no disponible", new RuntimeException()));
        return image;
    }

    private MultipartFile givenUnreadableImage() {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        try {
            when(image.getBytes()).thenThrow(new IOException("Disco lleno"));
        } catch (IOException ignored) {
        }
        return image;
    }

    private void stubReadableBytes(MultipartFile image) {
        try {
            when(image.getBytes()).thenReturn(new byte[]{1, 2, 3});
        } catch (IOException ignored) {
        }
    }

    private UserGoal pendingGoal() {
        return UserGoal.builder()
                .id(GOAL_ID).userId(USER_ID).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
    }

    private UserPlant activePlant() {
        return UserPlant.builder()
                .id(PLANT_ID).userId(USER_ID).plantNumber(1).requiredGoals(5)
                .status(PlantStatus.GROWING).startedAt(Instant.now()).build();
    }

    // --- act ---

    private CompleteUserGoalResponse complete() {
        return useCase.execute(new CompleteUserGoalRequest(GOAL_ID), null);
    }

    private CompleteUserGoalResponse completeWith(MultipartFile image) {
        return useCase.execute(new CompleteUserGoalRequest(GOAL_ID), image);
    }

    // --- assert ---

    private void thenGoalWasSavedAsCompleted() {
        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    private void thenResponseGoalIsCompleted(CompleteUserGoalResponse result) {
        assertThat(result.goal().status()).isEqualTo("COMPLETED");
    }

    private void thenResponseGoalIdentityPreserved(CompleteUserGoalResponse result) {
        assertThat(result.goal().id()).isEqualTo(GOAL_ID);
        assertThat(result.goal().userId()).isEqualTo(USER_ID);
        assertThat(result.goal().title()).isEqualTo("Meta");
    }

    private void thenCoinsCredited(int coins) {
        verify(coinService).credit(USER_ID, coins);
    }

    private void thenResponseImageUrlIs(CompleteUserGoalResponse result, String url) {
        assertThat(result.goal().imageUrl()).isEqualTo(url);
    }

    private void thenResponseHasNoImageUrl(CompleteUserGoalResponse result) {
        assertThat(result.goal().imageUrl()).isNull();
    }

    private void thenNothingWasSavedNorCredited() {
        verify(userGoalRepository, never()).save(any());
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    private void thenCompletedPlantWasFlushedBeforeNextPlantCreated() {
        InOrder inOrder = inOrder(userPlantRepository);
        inOrder.verify(userPlantRepository).countCompletedGoalsByPlantId(PLANT_ID);
        inOrder.verify(userPlantRepository).saveAndFlush(argThat((UserPlant plant) ->
                plant.getId().equals(PLANT_ID)
                        && plant.getStatus() == PlantStatus.COMPLETED
                        && plant.getCompletedAt() != null));
        inOrder.verify(userPlantRepository).save(argThat((UserPlant plant) ->
                plant.getId() == null
                        && plant.getPlantNumber().equals(2)
                        && plant.getStatus() == PlantStatus.GROWING));
    }

    private void thenHarvestResult(CompleteUserGoalResponse result) {
        assertThat(result.harvestTriggered()).isTrue();
        assertThat(result.currentPlant().id()).isEqualTo(2L);
    }

    private void thenCompleteMissingThrowsNotFound() {
        assertThatThrownBy(() -> useCase.execute(new CompleteUserGoalRequest(MISSING_ID), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenCompleteWithImageThrowsInvalidImage(MultipartFile image, String message) {
        assertThatThrownBy(() -> useCase.execute(new CompleteUserGoalRequest(GOAL_ID), image))
                .isInstanceOf(InvalidGoalImageException.class)
                .hasMessage(message);
    }

    private void thenCompleteWithImageThrowsInvalidImage(MultipartFile image) {
        assertThatThrownBy(() -> useCase.execute(new CompleteUserGoalRequest(GOAL_ID), image))
                .isInstanceOf(InvalidGoalImageException.class);
    }

    private void thenCompleteWithImageThrowsValidationUnavailable(MultipartFile image) {
        assertThatThrownBy(() -> useCase.execute(new CompleteUserGoalRequest(GOAL_ID), image))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }

    private void thenGoalWasNotSaved() {
        verify(userGoalRepository, never()).save(any());
    }

    private void thenNoCoinsCredited() {
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    private void thenValidationWasNotAttempted() {
        verify(imageValidationPort, never()).validate(any(), any(), any(), any());
    }

    private void thenImageWasNotStored() {
        verify(imageStorageService, never()).save(any());
    }
}
