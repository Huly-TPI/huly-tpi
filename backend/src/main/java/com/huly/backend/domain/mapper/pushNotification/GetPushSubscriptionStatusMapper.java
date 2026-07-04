package com.huly.backend.domain.mapper.pushNotification;

import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;

/**
 * Mapper de dominio para el caso de uso de estado de suscripcion push.
 */
public class GetPushSubscriptionStatusMapper {

    public GetPushSubscriptionStatusResponse toResponse(boolean subscribed, int notificationHour) {
        return new GetPushSubscriptionStatusResponse(subscribed, notificationHour);
    }
}
