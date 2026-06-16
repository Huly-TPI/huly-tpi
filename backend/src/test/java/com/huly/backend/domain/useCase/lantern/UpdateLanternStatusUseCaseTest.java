package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLanternStatusUseCaseTest {

    @Mock
    private LanternThoughtRepository lanternThoughtRepository;

    @InjectMocks
    private UpdateLanternStatusUseCase useCase;

    private LanternThought activeLantern() {
        return LanternThought.builder().id(1L).userId(10L).status(LanternStatus.ACTIVE).text("pensamiento").build();
    }

    @Test
    void execute_shouldUpdateToCompleted_whenThoughtIsActive() {
        LanternThought completed = LanternThought.builder().id(1L).status(LanternStatus.COMPLETED).build();
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(activeLantern()));
        when(lanternThoughtRepository.updateStatus(1L, LanternStatus.COMPLETED)).thenReturn(completed);

        LanternThought result = useCase.execute(1L, 10L, LanternStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(LanternStatus.COMPLETED);
        verify(lanternThoughtRepository).updateStatus(1L, LanternStatus.COMPLETED);
    }

    @Test
    void execute_shouldUpdateToCancelled_whenThoughtIsActive() {
        LanternThought cancelled = LanternThought.builder().id(1L).status(LanternStatus.CANCELLED).build();
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(activeLantern()));
        when(lanternThoughtRepository.updateStatus(1L, LanternStatus.CANCELLED)).thenReturn(cancelled);

        LanternThought result = useCase.execute(1L, 10L, LanternStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(LanternStatus.CANCELLED);
    }

    @Test
    void execute_shouldThrowIllegalStateException_whenThoughtIsNotActive() {
        LanternThought completed = LanternThought.builder().id(1L).userId(10L).status(LanternStatus.COMPLETED).build();
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> useCase.execute(1L, 10L, LanternStatus.CANCELLED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activo");
    }

    @Test
    void execute_shouldThrowIllegalArgumentException_whenNewStatusIsActive() {
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(activeLantern()));

        assertThatThrownBy(() -> useCase.execute(1L, 10L, LanternStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    @Test
    void execute_shouldThrowResourceNotFoundException_whenThoughtNotFound() {
        when(lanternThoughtRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 10L, LanternStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
