package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetUserBadgesUseCase {

    private final UserBadgeRepository userBadgeRepository;

    public List<UserBadge> execute(Long userId) {
        return userBadgeRepository.findAllByUserId(userId);
    }

}
