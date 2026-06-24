package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.CloudStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudThought {
    private Long id;
    private Long userId;
    private String text;
    private CloudStatus status;
    private boolean workedOn;
    private Instant createdAt;
}
