package com.huly.backend.infrastructure.presentation.dto.badge;

import java.time.Instant;
public record UserBadgeResponse(

    Long id, 
    BadgeResponse badge,
    Instant obtainedAt

) {
    
}
