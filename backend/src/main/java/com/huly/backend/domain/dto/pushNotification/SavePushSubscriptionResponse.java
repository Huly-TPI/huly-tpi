package com.huly.backend.domain.dto.pushNotification;

import java.time.LocalDateTime;

/**
 * Respuesta de dominio luego de intentar guardar una suscripcion push.
 * Si la suscripcion ya existia, {@code saved} es false y el resto de los campos son nulos.
 */
public record SavePushSubscriptionResponse(
        boolean saved,
        Long id,
        Long userId,
        String endpoint,
        String p256dh,
        String auth,
        LocalDateTime createdAt
) {

    public static SavePushSubscriptionResponse notSaved() {
        return new SavePushSubscriptionResponse(false, null, null, null, null, null, null);
    }
}
