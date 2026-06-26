package com.huly.backend.domain.dto.extension;

import java.util.List;

/**
 * Pedido de dominio para guardar las metricas de la extension de un usuario.
 *
 * @param userId  usuario al que pertenecen las metricas.
 * @param metrics metricas a guardar.
 */
public record SaveExtensionMetricsRequest(Long userId, List<ExtensionMetricItem> metrics) {
}
