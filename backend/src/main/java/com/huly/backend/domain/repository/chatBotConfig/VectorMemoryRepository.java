package com.huly.backend.domain.repository.chatBotConfig;

import com.huly.backend.domain.model.vector.VectorMemoryEntry;

import java.util.List;
import java.util.Optional;

public interface VectorMemoryRepository {
    List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId);
    Optional<String> findPersonalitySummaryByUserId(Long userId);
}
