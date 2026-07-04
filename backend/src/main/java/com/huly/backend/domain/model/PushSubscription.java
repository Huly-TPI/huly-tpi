package com.huly.backend.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PushSubscription {
     private Long id;
     private Long userId;
     private String endpoint;
     private String p256dh;
     private String auth;
     private LocalDateTime createdAt;
     @Builder.Default
     private int notificationHour = 9;
}
