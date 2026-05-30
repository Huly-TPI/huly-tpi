package com.huly.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private Instant createdAt;
    private Instant expiredAt;
}
