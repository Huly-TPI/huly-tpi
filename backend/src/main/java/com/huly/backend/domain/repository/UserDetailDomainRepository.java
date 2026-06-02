package com.huly.backend.domain.repository;

import java.util.Optional;

public interface UserDetailDomainRepository {
    Optional<Boolean> findProfileOnBoardingCompleted(Long userId);
    void completeOnboarding(Long userId, String answer1, String answer2, String answer3);
}
