package com.huly.backend.domain.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private Instant createdAt;
}
