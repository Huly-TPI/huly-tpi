package com.huly.backend.infrastructure.presentation.dto.pushNotification;
import lombok.Getter; 
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscribeRequest {
        private Long userId;
        private String endpoint;
        private String p256dh;
        private String auth;
}
