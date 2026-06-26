package com.huly.backend.infrastructure.presentation.mapper.notification;

import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsRequest;
import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de notificaciones por email:
 * traduce entre los inputs web y los DTOs de dominio.
 */
@Component
public class NotificationPresentationMapper {

    public UnsubscribeFromEmailsRequest toUnsubscribeRequest(String token) {
        return new UnsubscribeFromEmailsRequest(token);
    }

    public String toRedirectStatus(UnsubscribeFromEmailsResponse response) {
        return response.success() ? "ok" : "error";
    }
}
