package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.GoalStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoal {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private GoalStatus status;
    private Instant createdAt;
    private Long activityId;
    private Long userPlantId;
}
