package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.exception.InvalidGoalImageException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.model.goals.ImageValidationResult;
import com.huly.backend.domain.port.ImageValidationPort;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
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
class CompleteUserGoalUseCaseTest {

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

    @InjectMocks
    private CompleteUserGoalUseCase completeUserGoalUseCase;

    private UserGoal pendingGoal(Long id) {
        return UserGoal.builder()
                .id(id).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
    }

    private UserPlant activePlant() {
        return UserPlant.builder()
                .id(1L).userId(10L).plantNumber(1).requiredGoals(5)
                .status(PlantStatus.GROWING).startedAt(Instant.now()).build();
    }

    private MultipartFile mockImage(String returnedUrl) {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        try { when(image.getBytes()).thenReturn(new byte[]{1, 2, 3}); } catch (Exception ignored) {}
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenReturn(new ImageValidationResult(true, "La imagen es válida"));
        when(imageStorageService.save(image)).thenReturn(returnedUrl);
        return image;
    }

    @Test
    void execute_shouldSetStatusToCompleted_whenGoalExists() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L, null);

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(result.goal().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void execute_shouldNotModifyOtherFields_whenCompleting() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L, null);

        assertThat(result.goal().getId()).isEqualTo(1L);
        assertThat(result.goal().getUserId()).isEqualTo(10L);
        assertThat(result.goal().getTitle()).isEqualTo("Meta");
    }

    @Test
    void execute_shouldThrowNotFoundException_whenGoalDoesNotExist() {
        when(userGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userGoalRepository, never()).save(any());
    }

    @Test
    void execute_shouldCreditBaseCoins_whenNoImageProvided() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        completeUserGoalUseCase.execute(1L, null);

        verify(coinService).credit(10L, 10);
    }

    @Test
    void execute_shouldCreditBonusCoins_whenImageProvided() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);
        MultipartFile image = mockImage("/api/user-goals/images/photo.jpg");

        completeUserGoalUseCase.execute(1L, image);

        verify(coinService).credit(10L, 25);
    }

    @Test
    void execute_shouldSetImageUrl_whenImageProvided() {
        UserGoal goal = pendingGoal(1L);
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);
        MultipartFile image = mockImage("/api/user-goals/images/photo.jpg");

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L, image);

        assertThat(result.goal().getImageUrl()).isEqualTo("/api/user-goals/images/photo.jpg");
    }

    @Test
    void execute_shouldReturnEarlyWithoutSaving_whenGoalAlreadyCompleted() {
        UserGoal completed = UserGoal.builder()
                .id(1L).userId(10L).title("Meta").status(GoalStatus.COMPLETED)
                .createdAt(Instant.now()).build();
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(completed));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L, null);

        verify(userGoalRepository, never()).save(any());
        verify(coinService, never()).credit(anyLong(), anyInt());
        assertThat(result.goal().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void execute_shouldUseCustomCoinsReward_whenCoinsRewardIsSet() {
        UserGoal goal = UserGoal.builder()
                .id(1L).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .coinsReward(15).createdAt(Instant.now()).build();
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);

        completeUserGoalUseCase.execute(1L, null);

        verify(coinService).credit(10L, 15);
    }

    @Test
    void execute_shouldUseCustomCoinsRewardWithImage_whenCoinsRewardWithImageIsSet() {
        UserGoal goal = UserGoal.builder()
                .id(1L).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .coinsRewardWithImage(40).createdAt(Instant.now()).build();
        UserPlant plant = activePlant();
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(plant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(1L);
        MultipartFile image = mockImage("/api/user-goals/images/photo.jpg");

        completeUserGoalUseCase.execute(1L, image);

        verify(coinService).credit(10L, 40);
    }

    @Test
    void execute_shouldFlushCompletedPlantBeforeCreatingNextPlant_whenHarvestIsTriggered() {
        UserGoal goal = pendingGoal(1L);
        UserPlant currentPlant = activePlant();
        UserPlant nextPlant = UserPlant.builder()
                .id(2L).userId(10L).plantNumber(2).requiredGoals(8)
                .status(PlantStatus.GROWING).startedAt(Instant.now()).build();

        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(getOrCreateCurrentPlantUseCase.execute(10L)).thenReturn(currentPlant);
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.countCompletedGoalsByPlantId(1L)).thenReturn(5L);
        when(userPlantRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPlantRepository.save(argThat((UserPlant plant) -> plant.getId() == null))).thenReturn(nextPlant);

        CompleteUserGoalUseCase.Result result = completeUserGoalUseCase.execute(1L, null);

        InOrder inOrder = inOrder(userPlantRepository);
        inOrder.verify(userPlantRepository).countCompletedGoalsByPlantId(1L);
        inOrder.verify(userPlantRepository).saveAndFlush(argThat((UserPlant plant) ->
                plant.getId().equals(1L)
                        && plant.getStatus() == PlantStatus.COMPLETED
                        && plant.getCompletedAt() != null
        ));
        inOrder.verify(userPlantRepository).save(argThat((UserPlant plant) ->
                plant.getId() == null
                        && plant.getPlantNumber().equals(2)
                        && plant.getStatus() == PlantStatus.GROWING
        ));

        assertThat(result.harvestTriggered()).isTrue();
        assertThat(result.currentPlant().getId()).isEqualTo(2L);
    }

    @Test
    void execute_shouldThrowInvalidGoalImageException_whenFileSizeExceedsLimit() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getSize()).thenReturn(6L * 1024 * 1024);

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(InvalidGoalImageException.class)
                .hasMessage("La imagen no puede superar los 5 MB.");

        verify(imageValidationPort, never()).validate(any(), any(), any(), any());
        verify(userGoalRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowInvalidGoalImageException_whenMimeTypeIsInvalid() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("video/mp4");

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(InvalidGoalImageException.class);

        verify(userGoalRepository, never()).save(any());
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    @Test
    void execute_shouldThrowInvalidGoalImageException_whenMimeTypeIsNull() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn(null);

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(InvalidGoalImageException.class);

        verify(userGoalRepository, never()).save(any());
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    @Test
    void execute_shouldThrowInvalidGoalImageException_whenAiRejectsImage() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        try { when(image.getBytes()).thenReturn(new byte[]{1, 2, 3}); } catch (Exception ignored) {}
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenReturn(new ImageValidationResult(false, "La imagen no tiene relación con el reto"));

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(InvalidGoalImageException.class)
                .hasMessage("La imagen no tiene relación con el reto");

        verify(imageStorageService, never()).save(any());
        verify(userGoalRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowImageValidationUnavailableException_whenPortThrows() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        try { when(image.getBytes()).thenReturn(new byte[]{1, 2, 3}); } catch (Exception ignored) {}
        when(imageValidationPort.validate(any(), any(), any(), any()))
                .thenThrow(new ImageValidationUnavailableException("Servicio no disponible", new RuntimeException()));

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(ImageValidationUnavailableException.class);

        verify(userGoalRepository, never()).save(any());
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    @Test
    void execute_shouldThrowImageValidationUnavailableException_whenGetBytesThrows() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        try { when(image.getBytes()).thenThrow(new IOException("Disco lleno")); } catch (Exception ignored) {}

        assertThatThrownBy(() -> completeUserGoalUseCase.execute(1L, image))
                .isInstanceOf(ImageValidationUnavailableException.class);

        verify(imageValidationPort, never()).validate(any(), any(), any(), any());
        verify(userGoalRepository, never()).save(any());
    }
}
