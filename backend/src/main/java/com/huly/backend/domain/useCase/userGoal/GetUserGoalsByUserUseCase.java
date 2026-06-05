package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserGoalsByUserUseCase {

    private final UserGoalRepository userGoalRepository;
    private final UserRepository userRepository;

    public Page<UserGoal> executeCompleted(String email, Pageable pageable) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return userGoalRepository.findByUserIdAndStatus(user.getId(), GoalStatus.COMPLETED, pageable);
    }

    public Page<UserGoal> executePending(String email, Pageable pageable) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return userGoalRepository.findByUserIdAndStatus(user.getId(), GoalStatus.PENDING, pageable);
    }
}
