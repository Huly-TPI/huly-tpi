package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
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
