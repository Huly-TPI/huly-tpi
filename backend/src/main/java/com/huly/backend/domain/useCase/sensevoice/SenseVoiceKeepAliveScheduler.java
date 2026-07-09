package com.huly.backend.domain.useCase.sensevoice;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que mantiene despierto al microservicio SenseVoice pingueando {@code GET /health}.
 * Evita el cold start del scale-to-zero de Azure Container Apps (se apaga tras ~15 min sin tráfico).
 * <p>
 * Solo se activa con {@code app.sensevoice.keepalive.enabled=true} (por defecto está apagado, así en
 * local/dev no genera ruido al no existir el microservicio). El intervalo es configurable y por
 * defecto es de 10 minutos, con margen respecto a la ventana de 15.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sensevoice.keepalive.enabled", havingValue = "true")
public class SenseVoiceKeepAliveScheduler {

    private final AudioTranscriptionPort audioTranscriptionPort;

    @Scheduled(fixedRateString = "${app.sensevoice.keepalive.interval-ms:600000}", initialDelay = 60_000)
    public void keepAlive() {
        boolean ok = audioTranscriptionPort.ping();
        if (ok) {
            log.debug("SenseVoice keep-alive OK");
        } else {
            log.warn("SenseVoice keep-alive falló (microservicio no respondió)");
        }
    }
}
