package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.dto.badge.GetUserBadgesRequest;
import com.huly.backend.domain.dto.badge.GetUserBadgesResponse;
import com.huly.backend.domain.mapper.badge.GetUserBadgesMapper;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserBadgesUseCase {

    private final UserBadgeRepository userBadgeRepository;
    private final GetUserBadgesMapper mapper;

    public GetUserBadgesResponse execute(GetUserBadgesRequest request) {
        return mapper.toResponse(userBadgeRepository.findAllByUserId(request.userId()));
    }

}
