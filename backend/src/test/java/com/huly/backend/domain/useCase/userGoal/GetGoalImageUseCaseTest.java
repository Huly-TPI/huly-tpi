package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetGoalImageRequest;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetGoalImageUseCase")
class GetGoalImageUseCaseTest {

    private static final String FILENAME = "photo.jpg";
    private static final Path EXPECTED_PATH = Path.of("uploads/goals/photo.jpg");

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private GetGoalImageUseCase useCase;

    @Test
    @DisplayName("Delega la resolución de la ruta en el servicio de almacenamiento")
    void executeShouldDelegateToImageStorageService() {
        // --- arrange ---
        givenResolvedPath();
        // --- act ---
        Path result = getImage(FILENAME);
        // --- assert ---
        thenPathIsResolved(result);
    }

    // --- arrange ---

    private void givenResolvedPath() {
        when(imageStorageService.resolve(FILENAME)).thenReturn(EXPECTED_PATH);
    }

    // --- act ---

    private Path getImage(String filename) {
        return useCase.execute(new GetGoalImageRequest(filename));
    }

    // --- assert ---

    private void thenPathIsResolved(Path result) {
        assertThat(result).isEqualTo(EXPECTED_PATH);
        verify(imageStorageService).resolve(FILENAME);
    }
}
