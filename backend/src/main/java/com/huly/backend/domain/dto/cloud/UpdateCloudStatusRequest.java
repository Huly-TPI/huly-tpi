package com.huly.backend.domain.dto.cloud;

import com.huly.backend.domain.model.enums.CloudStatus;

/**
 * Pedido de dominio para actualizar el estado de un pensamiento de nube.
 *
 * @param id        identificador del pensamiento.
 * @param userId    usuario dueño del pensamiento.
 * @param newStatus nuevo estado a aplicar.
 */
public record UpdateCloudStatusRequest(Long id, Long userId, CloudStatus newStatus) {
}
