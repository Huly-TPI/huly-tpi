package com.huly.backend.domain.mapper.pushNotification;

import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsResponse;

/**
 * Mapper de dominio para el caso de uso de baja de emails de re-engagement.
 */
public class UnsubscribeFromEmailsMapper {

    public UnsubscribeFromEmailsResponse toResponse(boolean success) {
        return new UnsubscribeFromEmailsResponse(success);
    }
}
