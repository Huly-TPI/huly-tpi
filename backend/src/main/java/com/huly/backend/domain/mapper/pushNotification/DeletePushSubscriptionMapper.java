package com.huly.backend.domain.mapper.pushNotification;

import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionResponse;

/**
 * Mapper de dominio para el caso de uso de eliminacion de suscripcion push.
 */
public class DeletePushSubscriptionMapper {

    public DeletePushSubscriptionResponse toResponse() {
        return new DeletePushSubscriptionResponse(true);
    }
}
