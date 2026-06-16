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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkWorkedOnUseCaseTest {

    @Mock
    private LanternThoughtRepository lanternThoughtRepository;

    @InjectMocks
    private MarkWorkedOnUseCase useCase;

    @Test
    void execute_shouldCallMarkWorkedOn_whenThoughtExists() {
        LanternThought thought = LanternThought.builder().id(1L).userId(10L).status(LanternStatus.ACTIVE).build();
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(thought));

        useCase.execute(1L, 10L);

        verify(lanternThoughtRepository).markWorkedOn(1L);
    }

    @Test
    void execute_shouldVerifyOwnership_beforeMarkingWorkedOn() {
        LanternThought thought = LanternThought.builder().id(1L).userId(10L).build();
        when(lanternThoughtRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(thought));

        useCase.execute(1L, 10L);

        verify(lanternThoughtRepository).findByIdAndUserId(1L, 10L);
        verify(lanternThoughtRepository).markWorkedOn(1L);
    }

    @Test
    void execute_shouldThrowResourceNotFoundException_whenThoughtNotFound() {
        when(lanternThoughtRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
