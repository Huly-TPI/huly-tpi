package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.user.UserBadge;

import java.util.List;

public interface UserBadgeRepository {
    List<UserBadge> findAllByUserId(Long userId);
    boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode);
    UserBadge save(UserBadge userBadge);
}