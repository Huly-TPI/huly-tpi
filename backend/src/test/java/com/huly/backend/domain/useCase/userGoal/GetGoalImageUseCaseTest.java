package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetGoalImageRequest;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetGoalImageUseCaseTest {

    @Test
    void execute_shouldDelegateToImageStorageService() {
        ImageStorageService imageStorageService = mock(ImageStorageService.class);
        Path expected = Path.of("uploads/goals/photo.jpg");
        when(imageStorageService.resolve("photo.jpg")).thenReturn(expected);
        GetGoalImageUseCase useCase = new GetGoalImageUseCase(imageStorageService);

        assertThat(useCase.execute(new GetGoalImageRequest("photo.jpg"))).isEqualTo(expected);
        verify(imageStorageService).resolve("photo.jpg");
    }
}
