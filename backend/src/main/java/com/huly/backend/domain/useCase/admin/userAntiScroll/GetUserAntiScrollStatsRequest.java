package com.huly.backend.domain.useCase.admin.userAntiScroll;

public record GetUserAntiScrollStatsRequest(
        Long userId,
        String week,
        String day
) {
}
