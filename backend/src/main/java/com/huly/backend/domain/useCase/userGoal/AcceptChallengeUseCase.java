package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AcceptChallengeUseCase {

    private final UserGoalRepository userGoalRepository;

    public UserGoal execute(Long userId, String title, String description, Long activityId) {
       
        UserGoal userGoal = UserGoal.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .activityId(activityId)
                .status(GoalStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        return userGoalRepository.save(userGoal);
    }
}
