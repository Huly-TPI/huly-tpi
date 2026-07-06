package com.huly.backend.domain.dto.pending;

public record UpdatePendingPositionRequest(Long id, Long userId, double positionX, double positionY) {}
