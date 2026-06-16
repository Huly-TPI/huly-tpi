package com.huly.backend.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserPlan {
    private Long id;
    private Long userId;
    private String planCode;
    private Instant grantedAt;
    private Instant expiresAt;

    public boolean isActive(Instant now) {
        return expiresAt != null && expiresAt.isAfter(now);
    }
}
