package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;

import java.util.List;
import java.util.Optional;

public interface LanternThoughtRepository {
    LanternThought save(Long userId, String text);
    List<LanternThought> findAllByUserId(Long userId);
    Optional<LanternThought> findByIdAndUserId(Long id, Long userId);
    LanternThought updateStatus(Long id, LanternStatus status);
    void markWorkedOn(Long id);
}
