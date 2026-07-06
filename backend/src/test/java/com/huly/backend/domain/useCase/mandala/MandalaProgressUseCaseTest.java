package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.mandala.ClearMandalaProgressMapper;
import com.huly.backend.domain.mapper.mandala.GetMandalaProgressMapper;
import com.huly.backend.domain.mapper.mandala.GetMandalaSessionStatusMapper;
import com.huly.backend.domain.mapper.mandala.SaveMandalaProgressMapper;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

class MandalaProgressUseCaseTest {

    private static final Long USER_ID = 7L;

    private MandalaProgressRepository mandalaProgressRepository;
    private MandalaService mandalaService;
    private SaveMandalaProgressUseCase saveUseCase;
    private GetMandalaProgressUseCase getUseCase;
    private GetMandalaSessionStatusUseCase getSessionStatusUseCase;
    private ClearMandalaProgressUseCase clearUseCase;

    @BeforeEach
    void setUp() {
        mandalaProgressRepository = mock(MandalaProgressRepository.class);
        mandalaService = mock(MandalaService.class);
        saveUseCase = new SaveMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new SaveMandalaProgressMapper());
        getUseCase = new GetMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new GetMandalaProgressMapper());
        getSessionStatusUseCase = new GetMandalaSessionStatusUseCase(
                mandalaProgressRepository,
                mandalaService,
                new GetMandalaSessionStatusMapper());
        clearUseCase = new ClearMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new ClearMandalaProgressMapper());

        doNothing().when(mandalaService).validateMandalaAvailability(USER_ID, "mandala-01");
        doThrow(new ResourceNotFoundException("Mandala no disponible"))
                .when(mandalaService).validateMandalaAvailability(USER_ID, "mandala-99");
    }

    @Test
    void saveProgress_persistsOnlyWhenMandalaIsAvailable() {
        byte[] paintBlob = "paint".getBytes();

        saveUseCase.execute(new SaveMandalaProgressRequest(USER_ID, "mandala-01", paintBlob));

        verify(mandalaProgressRepository).save(any(MandalaProgress.class));
    }

    @Test
    void saveProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> saveUseCase.execute(
                new SaveMandalaProgressRequest(USER_ID, "mandala-99", "paint".getBytes())))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
        verify(mandalaProgressRepository, never()).save(any());
    }

    @Test
    void getProgress_returnsStoredBlobForAvailableMandala() {
        byte[] paintBlob = "paint".getBytes();
        when(mandalaProgressRepository.findByUserIdAndMandalaId(USER_ID, "mandala-01"))
                .thenReturn(Optional.of(MandalaProgress.builder()
                        .userId(USER_ID)
                        .mandalaId("mandala-01")
                        .paintBlob(paintBlob)
                        .sessionRegistered(false)
                        .build()));

        assertThat(getUseCase.execute(new GetMandalaProgressRequest(USER_ID, "mandala-01")).paintBlob())
                .contains(paintBlob);
    }

    @Test
    void getProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> getUseCase.execute(new GetMandalaProgressRequest(USER_ID, "mandala-99")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
        verify(mandalaProgressRepository, never()).findByUserIdAndMandalaId(any(), any());
    }

    @Test
    void clearProgress_deletesOnlyWhenMandalaIsAvailable() {
        clearUseCase.execute(new ClearMandalaProgressRequest(USER_ID, "mandala-01"));

        verify(mandalaProgressRepository).deleteByUserIdAndMandalaId(USER_ID, "mandala-01");
    }

    @Test
    void clearProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> clearUseCase.execute(new ClearMandalaProgressRequest(USER_ID, "mandala-99")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
        verify(mandalaProgressRepository, never()).deleteByUserIdAndMandalaId(any(), any());
    }

    @Test
    void getSessionStatus_returnsStoredFlagForAvailableMandala() {
        when(mandalaProgressRepository.findByUserIdAndMandalaId(USER_ID, "mandala-01"))
                .thenReturn(Optional.of(MandalaProgress.builder()
                        .userId(USER_ID)
                        .mandalaId("mandala-01")
                        .paintBlob("paint".getBytes())
                        .sessionRegistered(true)
                        .build()));

        assertThat(getSessionStatusUseCase.execute(
                new GetMandalaSessionStatusRequest(USER_ID, "mandala-01"))
                .sessionRegistered()).isTrue();
    }
}
