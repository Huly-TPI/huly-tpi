package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.UserPersonalitySummary;

import java.util.Optional;

public interface UserPersonalitySummaryRepository {

    Optional<UserPersonalitySummary> findByUserId(Long userId);

    UserPersonalitySummary save(UserPersonalitySummary summary);

    void deleteByUserId(Long userId);
}
