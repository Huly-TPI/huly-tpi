package com.huly.backend.domain.useCase.sensevoice;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenseVoiceKeepAliveSchedulerTest {

    @Mock
    private AudioTranscriptionPort audioTranscriptionPort;

    @InjectMocks
    private SenseVoiceKeepAliveScheduler scheduler;

    @Test
    @DisplayName("Pinguea el microservicio en cada ejecución")
    void keepAliveShouldPingMicroservice() {
        // --- arrange ---
        givenPingSucceeds();
        // --- act ---
        keepAlive();
        // --- assert ---
        thenMicroserviceWasPinged();
    }

    @Test
    @DisplayName("No propaga excepción cuando el microservicio no responde")
    void keepAliveShouldNotFailWhenPingReturnsFalse() {
        // --- arrange ---
        givenPingFails();
        // --- act ---
        keepAlive();
        // --- assert ---
        thenMicroserviceWasPinged();
    }

    // --- arrange ---

    private void givenPingSucceeds() {
        when(audioTranscriptionPort.ping()).thenReturn(true);
    }

    private void givenPingFails() {
        when(audioTranscriptionPort.ping()).thenReturn(false);
    }

    // --- act ---

    private void keepAlive() {
        scheduler.keepAlive();
    }

    // --- assert ---

    private void thenMicroserviceWasPinged() {
        verify(audioTranscriptionPort).ping();
    }
}
