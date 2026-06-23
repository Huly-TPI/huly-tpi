package com.huly.backend.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserPersonalitySummary {

    private final Long id;
    private final Long userId;
    private final String summary;
    private final String accepted;
    private final String rejected;
    private final Instant generatedAt;
    private final Instant updatedAt;
}
