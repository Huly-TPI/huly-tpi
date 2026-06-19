package com.huly.backend.domain.model.user;

import com.huly.backend.domain.model.enums.PlantStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlant {
    private Long id;
    private Long userId;
    private Integer plantNumber;
    private Integer requiredGoals;
    private PlantStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Long completedGoalsCount;
}
