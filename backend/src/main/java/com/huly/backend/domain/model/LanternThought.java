package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.LanternStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanternThought {
    private Long id;
    private Long userId;
    private String text;
    private LanternStatus status;
    private boolean workedOn;
    private Instant createdAt;
}
