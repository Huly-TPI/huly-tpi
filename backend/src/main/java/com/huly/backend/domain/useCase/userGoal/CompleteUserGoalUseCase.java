package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public class CompleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;
    private final CoinService coinService;
    private final ImageStorageService imageStorageService;

    @Transactional
    public UserGoal execute(Long id, MultipartFile image) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            return goal;
        }

        String imageUrl = image != null && !image.isEmpty() ? imageStorageService.save(image) : null;

        goal.setStatus(GoalStatus.COMPLETED);
        goal.setImageUrl(imageUrl);

        UserGoal saved = userGoalRepository.save(goal);

        int coins = imageUrl != null
                ? (goal.getCoinsRewardWithImage() != null ? goal.getCoinsRewardWithImage() : 25)
                : (goal.getCoinsReward() != null ? goal.getCoinsReward() : 10);
        coinService.credit(goal.getUserId(), coins);

        return saved;
    }
}
