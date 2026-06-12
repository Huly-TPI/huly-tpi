package com.huly.backend.infrastructure.presentation.dto.user;

import java.time.Instant;

public record MembershipResponse(boolean active, String planCode, String productId, Instant expiresAt) {

    public static MembershipResponse inactive() {
        return new MembershipResponse(false, null, null, null);
    }
}
