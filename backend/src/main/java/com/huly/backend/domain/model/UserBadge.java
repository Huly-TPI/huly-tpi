package com.huly.backend.domain.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {
    private Long id;
    private Long userId;
    private Badge badge;
    private Instant obtainedAt;
}
