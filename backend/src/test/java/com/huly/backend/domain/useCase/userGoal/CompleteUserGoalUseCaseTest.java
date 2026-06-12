package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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
    private CoinService coinService;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private CompleteUserGoalUseCase completeUserGoalUseCase;

    private UserGoal pendingGoal(Long id) {
        return UserGoal.builder()
                .id(id).userId(10L).title("Meta").status(GoalStatus.PENDING)
                .createdAt(Instant.now()).build();
    }

    private MultipartFile mockImage(String returnedUrl) {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(imageStorageService.save(image)).thenReturn(returnedUrl);
        return image;
    }

    @Test
    void execute_shouldSetStatusToCompleted_whenGoalExists() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = completeUserGoalUseCase.execute(1L, null);

        ArgumentCaptor<UserGoal> captor = ArgumentCaptor.forClass(UserGoal.class);
        verify(userGoalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(result.getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void execute_shouldNotModifyOtherFields_whenCompleting() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = completeUserGoalUseCase.execute(1L, null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Meta");
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
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        completeUserGoalUseCase.execute(1L, null);

        verify(coinService).credit(10L, 10);
    }

    @Test
    void execute_shouldCreditBonusCoins_whenImageProvided() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MultipartFile image = mockImage("/api/user-goals/images/photo.jpg");

        completeUserGoalUseCase.execute(1L, image);

        verify(coinService).credit(10L, 25);
    }

    @Test
    void execute_shouldSetImageUrl_whenImageProvided() {
        UserGoal goal = pendingGoal(1L);
        when(userGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(userGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MultipartFile image = mockImage("/api/user-goals/images/photo.jpg");

        UserGoal result = completeUserGoalUseCase.execute(1L, image);

        assertThat(result.getImageUrl()).isEqualTo("/api/user-goals/images/photo.jpg");
    }
}
