package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.mapper.userGoal.GetUserGoalsMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class GetUserGoalsByUserUseCase {

    private final UserGoalRepository userGoalRepository;
    private final GetUserGoalsMapper mapper;

    public GetUserGoalsResponse execute(GetUserGoalsRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
        Page<UserGoal> completados =
                userGoalRepository.findByUserIdAndStatus(request.userId(), GoalStatus.COMPLETED, pageable);
        Page<UserGoal> pendientes =
                userGoalRepository.findByUserIdAndStatus(request.userId(), GoalStatus.PENDING, pageable);
        return mapper.toResponse(completados, pendientes);
    }
}
