package com.huly.backend.infrastructure.presentation.dto.user;

import java.time.Instant;

public record MembershipResponse(boolean active, String planCode, Instant expiresAt) {

    public static MembershipResponse inactive() {
        return new MembershipResponse(false, null, null);
    }
}
