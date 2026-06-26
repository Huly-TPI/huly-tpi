package com.huly.backend.domain.dto.extension;

import java.util.List;

/**
 * Pedido de dominio para guardar la configuracion anti-scroll de un usuario.
 *
 * @param userId               usuario al que pertenece la configuracion.
 * @param enabled              si el modo anti-scroll esta activado.
 * @param pauseIntervalSeconds intervalo de pausa en segundos.
 * @param monitoredDomains     dominios monitoreados.
 * @param dataSharingConsent   consentimiento para compartir datos.
 */
public record SaveUserAntiScrollSettingsRequest(
        Long userId,
        boolean enabled,
        int pauseIntervalSeconds,
        List<String> monitoredDomains,
        boolean dataSharingConsent
) {
}
