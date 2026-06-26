package com.huly.backend.domain.useCase.userGoal;


import com.huly.backend.domain.dto.userGoal.AddUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.mapper.userGoal.AddUserGoalMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;
    private final AddUserGoalMapper mapper;

    public AddUserGoalResponse execute(AddUserGoalRequest request) {
        UserGoal saved = userGoalRepository.save(mapper.toModel(request));
        return mapper.toResponse(saved);
    }
}
