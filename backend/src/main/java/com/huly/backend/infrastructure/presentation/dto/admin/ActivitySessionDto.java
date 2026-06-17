package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ActivitySessionDto {
    private Long id;
    private String activityType;
    private Instant createdAt;
}
