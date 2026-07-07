package com.huly.backend.domain.service.pending;

import java.util.List;

public record TaskBalanceRecommendationResult(List<Long> recommendedTaskIds, double totalLoadUsed, double budget) {}
