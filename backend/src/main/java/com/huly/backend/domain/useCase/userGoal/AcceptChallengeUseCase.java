package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AcceptChallengeUseCase {

    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    public UserGoal execute(String email, String title, String description, Long activityId) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        UserGoal userGoal = UserGoal.builder()
                .userId(user.getId())
                .title(title)
                .description(description)
                .activityId(activityId)
                .status(GoalStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        return userGoalRepository.save(userGoal);
    }
}
