package com.huly.backend.domain.dto.extension;

/**
 * Representacion de una metrica de la extension dentro de un pedido de dominio.
 */
public record ExtensionMetricItem(
        String domain,
        int activeSeconds,
        int scrollCount,
        int modalsShown,
        int redirects
) {
}
