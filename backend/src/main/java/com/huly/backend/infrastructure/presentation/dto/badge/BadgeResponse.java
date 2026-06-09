package com.huly.backend.infrastructure.presentation.dto.badge;
import java.time.Instant;
public record BadgeResponse( 
    Long id,
    String code,
    String name,
    String description,
    String imageUrl,
    Instant createdAt
) {
}
