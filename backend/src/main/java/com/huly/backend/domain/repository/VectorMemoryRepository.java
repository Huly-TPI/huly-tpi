package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.vector.VectorMemoryEntry;

import java.util.List;

public interface VectorMemoryRepository {
    List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId);
}
