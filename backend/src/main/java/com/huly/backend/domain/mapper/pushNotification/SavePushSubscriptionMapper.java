package com.huly.backend.domain.mapper.pushNotification;

import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionResponse;
import com.huly.backend.domain.model.PushSubscription;

import java.time.LocalDateTime;

/**
 * Mapper de dominio para el caso de uso de guardado de suscripcion push.
 */
public class SavePushSubscriptionMapper {

    public PushSubscription toModel(SavePushSubscriptionRequest request) {
        return PushSubscription.builder()
                .userId(request.userId())
                .endpoint(request.endpoint())
                .p256dh(request.p256dh())
                .auth(request.auth())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public SavePushSubscriptionResponse toResponse(PushSubscription subscription) {
        return new SavePushSubscriptionResponse(
                true,
                subscription.getId(),
                subscription.getUserId(),
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                subscription.getCreatedAt()
        );
    }
}
