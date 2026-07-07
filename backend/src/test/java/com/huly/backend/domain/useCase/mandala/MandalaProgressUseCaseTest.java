package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test agrupado que cubre los cuatro casos de uso de progreso de mandala:
 * guardar, obtener, consultar estado de sesion y limpiar.
 */
@ExtendWith(MockitoExtension.class)
class MandalaProgressUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final String AVAILABLE_MANDALA = "mandala-01";
    private static final String UNAVAILABLE_MANDALA = "mandala-99";
    private static final byte[] PAINT_BLOB = "paint".getBytes();

    @Mock
    private MandalaProgressRepository mandalaProgressRepository;

    @Mock
    private MandalaService mandalaService;

    private SaveMandalaProgressUseCase saveUseCase;
    private GetMandalaProgressUseCase getUseCase;
    private GetMandalaSessionStatusUseCase getSessionStatusUseCase;
    private ClearMandalaProgressUseCase clearUseCase;

    @BeforeEach
    void setUp() {
        saveUseCase = new SaveMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new SaveMandalaProgressMapper());
        getUseCase = new GetMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new GetMandalaProgressMapper());
        getSessionStatusUseCase = new GetMandalaSessionStatusUseCase(mandalaProgressRepository, mandalaService,
                new GetMandalaSessionStatusMapper());
        clearUseCase = new ClearMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                new ClearMandalaProgressMapper());
    }

    @Test
    @DisplayName("Persiste el progreso cuando el mandala esta disponible")
    void saveProgressPersistsWhenMandalaIsAvailable() {
        // --- arrange ---
        givenMandalaIsAvailable();

        // --- act ---
        saveAvailableProgress();

        // --- assert ---
        thenProgressWasSaved();
    }

    @Test
    @DisplayName("Rechaza guardar cuando el mandala no esta disponible")
    void saveProgressRejectsUnavailableMandala() {
        // --- arrange ---
        givenMandalaIsUnavailable();

        // --- act & assert ---
        thenSaveThrowsResourceNotFound();
        thenProgressWasNotSaved();
    }

    @Test
    @DisplayName("Devuelve el blob almacenado cuando el mandala esta disponible")
    void getProgressReturnsStoredBlobWhenMandalaIsAvailable() {
        // --- arrange ---
        givenMandalaIsAvailable();
        givenStoredProgressWithBlob();

        // --- act ---
        GetMandalaProgressResponse result = getAvailableProgress();

        // --- assert ---
        thenResponseContainsBlob(result);
    }

    @Test
    @DisplayName("Devuelve progreso vacio cuando no hay nada almacenado")
    void getProgressReturnsEmptyWhenNoStoredProgress() {
        // --- arrange ---
        givenMandalaIsAvailable();
        givenNoStoredProgress();

        // --- act ---
        GetMandalaProgressResponse result = getAvailableProgress();

        // --- assert ---
        thenResponseHasNoBlob(result);
    }

    @Test
    @DisplayName("Rechaza obtener progreso cuando el mandala no esta disponible")
    void getProgressRejectsUnavailableMandala() {
        // --- arrange ---
        givenMandalaIsUnavailable();

        // --- act & assert ---
        thenGetProgressThrowsResourceNotFound();
        thenProgressWasNotQueried();
    }

    @Test
    @DisplayName("Devuelve sesion registrada cuando el progreso lo indica")
    void getSessionStatusReturnsTrueWhenSessionRegistered() {
        // --- arrange ---
        givenMandalaIsAvailable();
        givenStoredProgressWithSession(true);

        // --- act ---
        GetMandalaSessionStatusResponse result = getAvailableSessionStatus();

        // --- assert ---
        thenSessionIsRegistered(result);
    }

    @Test
    @DisplayName("Devuelve sesion no registrada cuando no hay progreso almacenado")
    void getSessionStatusReturnsFalseWhenNoStoredProgress() {
        // --- arrange ---
        givenMandalaIsAvailable();
        givenNoStoredProgress();

        // --- act ---
        GetMandalaSessionStatusResponse result = getAvailableSessionStatus();

        // --- assert ---
        thenSessionIsNotRegistered(result);
    }

    @Test
    @DisplayName("Rechaza obtener el estado de sesion cuando el mandala no esta disponible")
    void getSessionStatusRejectsUnavailableMandala() {
        // --- arrange ---
        givenMandalaIsUnavailable();

        // --- act & assert ---
        thenGetSessionStatusThrowsResourceNotFound();
        thenProgressWasNotQueried();
    }

    @Test
    @DisplayName("Elimina el progreso cuando el mandala esta disponible")
    void clearProgressDeletesWhenMandalaIsAvailable() {
        // --- arrange ---
        givenMandalaIsAvailable();

        // --- act ---
        clearAvailableProgress();

        // --- assert ---
        thenProgressWasDeleted();
    }

    @Test
    @DisplayName("Rechaza eliminar cuando el mandala no esta disponible")
    void clearProgressRejectsUnavailableMandala() {
        // --- arrange ---
        givenMandalaIsUnavailable();

        // --- act & assert ---
        thenClearThrowsResourceNotFound();
        thenProgressWasNotDeleted();
    }

    // --- arrange ---

    private void givenMandalaIsAvailable() {
        doNothing().when(mandalaService).validateMandalaAvailability(USER_ID, AVAILABLE_MANDALA);
    }

    private void givenMandalaIsUnavailable() {
        doThrow(new ResourceNotFoundException("Mandala no disponible"))
                .when(mandalaService).validateMandalaAvailability(USER_ID, UNAVAILABLE_MANDALA);
    }

    private void givenStoredProgressWithBlob() {
        when(mandalaProgressRepository.findByUserIdAndMandalaId(USER_ID, AVAILABLE_MANDALA))
                .thenReturn(Optional.of(MandalaProgress.builder()
                        .userId(USER_ID)
                        .mandalaId(AVAILABLE_MANDALA)
                        .paintBlob(PAINT_BLOB)
                        .sessionRegistered(false)
                        .build()));
    }

    private void givenStoredProgressWithSession(boolean sessionRegistered) {
        when(mandalaProgressRepository.findByUserIdAndMandalaId(USER_ID, AVAILABLE_MANDALA))
                .thenReturn(Optional.of(MandalaProgress.builder()
                        .userId(USER_ID)
                        .mandalaId(AVAILABLE_MANDALA)
                        .paintBlob(PAINT_BLOB)
                        .sessionRegistered(sessionRegistered)
                        .build()));
    }

    private void givenNoStoredProgress() {
        when(mandalaProgressRepository.findByUserIdAndMandalaId(USER_ID, AVAILABLE_MANDALA))
                .thenReturn(Optional.empty());
    }

    // --- act ---

    private void saveAvailableProgress() {
        saveUseCase.execute(new SaveMandalaProgressRequest(USER_ID, AVAILABLE_MANDALA, PAINT_BLOB));
    }

    private void saveUnavailableProgress() {
        saveUseCase.execute(new SaveMandalaProgressRequest(USER_ID, UNAVAILABLE_MANDALA, PAINT_BLOB));
    }

    private GetMandalaProgressResponse getAvailableProgress() {
        return getUseCase.execute(new GetMandalaProgressRequest(USER_ID, AVAILABLE_MANDALA));
    }

    private void getUnavailableProgress() {
        getUseCase.execute(new GetMandalaProgressRequest(USER_ID, UNAVAILABLE_MANDALA));
    }

    private GetMandalaSessionStatusResponse getAvailableSessionStatus() {
        return getSessionStatusUseCase.execute(new GetMandalaSessionStatusRequest(USER_ID, AVAILABLE_MANDALA));
    }

    private void getUnavailableSessionStatus() {
        getSessionStatusUseCase.execute(new GetMandalaSessionStatusRequest(USER_ID, UNAVAILABLE_MANDALA));
    }

    private void clearAvailableProgress() {
        clearUseCase.execute(new ClearMandalaProgressRequest(USER_ID, AVAILABLE_MANDALA));
    }

    private void clearUnavailableProgress() {
        clearUseCase.execute(new ClearMandalaProgressRequest(USER_ID, UNAVAILABLE_MANDALA));
    }

    // --- assert ---

    private void thenProgressWasSaved() {
        ArgumentCaptor<MandalaProgress> captor = ArgumentCaptor.forClass(MandalaProgress.class);
        verify(mandalaProgressRepository).save(captor.capture());
        MandalaProgress saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getMandalaId()).isEqualTo(AVAILABLE_MANDALA);
        assertThat(saved.getPaintBlob()).isEqualTo(PAINT_BLOB);
    }

    private void thenProgressWasNotSaved() {
        verify(mandalaProgressRepository, never()).save(any());
    }

    private void thenResponseContainsBlob(GetMandalaProgressResponse response) {
        assertThat(response.paintBlob()).contains(PAINT_BLOB);
    }

    private void thenResponseHasNoBlob(GetMandalaProgressResponse response) {
        assertThat(response.paintBlob()).isEmpty();
    }

    private void thenSessionIsRegistered(GetMandalaSessionStatusResponse response) {
        assertThat(response.sessionRegistered()).isTrue();
    }

    private void thenSessionIsNotRegistered(GetMandalaSessionStatusResponse response) {
        assertThat(response.sessionRegistered()).isFalse();
    }

    private void thenProgressWasNotQueried() {
        verify(mandalaProgressRepository, never()).findByUserIdAndMandalaId(any(), any());
    }

    private void thenProgressWasDeleted() {
        verify(mandalaProgressRepository).deleteByUserIdAndMandalaId(USER_ID, AVAILABLE_MANDALA);
    }

    private void thenProgressWasNotDeleted() {
        verify(mandalaProgressRepository, never()).deleteByUserIdAndMandalaId(any(), any());
    }

    private void thenSaveThrowsResourceNotFound() {
        assertThatThrownBy(this::saveUnavailableProgress)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }

    private void thenGetProgressThrowsResourceNotFound() {
        assertThatThrownBy(this::getUnavailableProgress)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }

    private void thenGetSessionStatusThrowsResourceNotFound() {
        assertThatThrownBy(this::getUnavailableSessionStatus)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }

    private void thenClearThrowsResourceNotFound() {
        assertThatThrownBy(this::clearUnavailableProgress)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }
}
