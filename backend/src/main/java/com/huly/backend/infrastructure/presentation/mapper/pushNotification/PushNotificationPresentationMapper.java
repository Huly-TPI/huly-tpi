package com.huly.backend.infrastructure.presentation.mapper.pushNotification;

import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.PushSubscriptionStatusResponse;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.SubscribeRequest;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.UnsubscribeRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de push notifications:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class PushNotificationPresentationMapper {

    public SavePushSubscriptionRequest toSaveRequest(SubscribeRequest request) {
        return new SavePushSubscriptionRequest(
                request.getUserId(),
                request.getEndpoint(),
                request.getP256dh(),
                request.getAuth()
        );
    }

    public DeletePushSubscriptionRequest toDeleteRequest(UnsubscribeRequest request) {
        return new DeletePushSubscriptionRequest(request.getEndpoint());
    }

    public GetPushSubscriptionStatusRequest toStatusRequest(Long userId) {
        return new GetPushSubscriptionStatusRequest(userId);
    }

    public PushSubscriptionStatusResponse toStatusResponse(GetPushSubscriptionStatusResponse response) {
        return new PushSubscriptionStatusResponse(response.subscribed());
    }
}
