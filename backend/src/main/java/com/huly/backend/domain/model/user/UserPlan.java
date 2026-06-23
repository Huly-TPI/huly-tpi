package com.huly.backend.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserPlan {
    private Long id;
    private Long userId;
    private Long productId;
    private String planCode;
    private Instant grantedAt;
    private Instant expiresAt;
    private Instant expiryReminderSentFor;

    public boolean isActive(Instant now) {
        return expiresAt != null && expiresAt.isAfter(now);
    }
}
