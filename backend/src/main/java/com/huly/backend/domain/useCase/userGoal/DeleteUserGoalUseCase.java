package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;

    public void execute(Long id) {
        if (!userGoalRepository.existsById(id)) {
            throw new NotFoundException("UserGoal", "id", id);
        }
        userGoalRepository.deleteById(id);
    }
}
