package com.huly.backend.domain.model.user;

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
    private String imageUrl;
    private Integer coinsReward = 10;
    private Integer coinsRewardWithImage = 25;
}
