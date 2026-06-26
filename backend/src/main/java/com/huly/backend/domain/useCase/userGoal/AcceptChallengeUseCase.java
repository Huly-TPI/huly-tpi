package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.mapper.userGoal.AcceptChallengeMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AcceptChallengeUseCase {

    private final UserGoalRepository userGoalRepository;
    private final AcceptChallengeMapper mapper;

    public AcceptChallengeResponse execute(AcceptChallengeRequest request) {
        UserGoal saved = userGoalRepository.save(mapper.toModel(request));
        return mapper.toResponse(saved);
    }
}
