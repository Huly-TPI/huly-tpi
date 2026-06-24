package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;

import java.util.List;
import java.util.Optional;

public interface CloudThoughtRepository {
    CloudThought save(Long userId, String text);
    List<CloudThought> findAllByUserId(Long userId);
    Optional<CloudThought> findByIdAndUserId(Long id, Long userId);
    CloudThought updateStatus(Long id, CloudStatus status);
    void markWorkedOn(Long id);
}
