package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MandalaProgressUseCaseTest {

    private static final Long USER_ID = 7L;

    private MandalaProgressRepository mandalaProgressRepository;
    private ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private SaveMandalaProgressUseCase saveUseCase;
    private GetMandalaProgressUseCase getUseCase;
    private ClearMandalaProgressUseCase clearUseCase;

    @BeforeEach
    void setUp() {
        mandalaProgressRepository = mock(MandalaProgressRepository.class);
        listAvailableMandalasUseCase = mock(ListAvailableMandalasUseCase.class);
        saveUseCase = new SaveMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);
        getUseCase = new GetMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);
        clearUseCase = new ClearMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);

        when(listAvailableMandalasUseCase.execute(USER_ID)).thenReturn(List.of(availableMandala("mandala-01")));
    }

    @Test
    void saveProgress_persistsOnlyWhenMandalaIsAvailable() {
        byte[] paintBlob = "paint".getBytes();

        saveUseCase.execute(USER_ID, "mandala-01", paintBlob);

        verify(mandalaProgressRepository).save(any(MandalaProgress.class));
    }

    @Test
    void saveProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> saveUseCase.execute(USER_ID, "mandala-99", "paint".getBytes()))
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
                        .build()));

        assertThat(getUseCase.execute(USER_ID, "mandala-01")).contains(paintBlob);
    }

    @Test
    void getProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> getUseCase.execute(USER_ID, "mandala-99"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
        verify(mandalaProgressRepository, never()).findByUserIdAndMandalaId(any(), any());
    }

    @Test
    void clearProgress_deletesOnlyWhenMandalaIsAvailable() {
        clearUseCase.execute(USER_ID, "mandala-01");

        verify(mandalaProgressRepository).deleteByUserIdAndMandalaId(USER_ID, "mandala-01");
    }

    @Test
    void clearProgress_rejectsUnavailableMandala() {
        assertThatThrownBy(() -> clearUseCase.execute(USER_ID, "mandala-99"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
        verify(mandalaProgressRepository, never()).deleteByUserIdAndMandalaId(any(), any());
    }

    private AvailableMandala availableMandala(String id) {
        return AvailableMandala.builder()
                .mandala(Mandala.builder()
                        .id(id)
                        .title(id)
                        .description("desc")
                        .assetKey(id)
                        .displayOrder(1)
                        .active(true)
                        .accessType(MandalaAccessType.FREE)
                        .build())
                .unlockSource(MandalaUnlockSource.FREE)
                .build();
    }
}
