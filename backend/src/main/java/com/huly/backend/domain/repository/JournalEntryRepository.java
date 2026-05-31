package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;

public interface JournalEntryRepository {
    JournalEntry save(Long userId, String content, Mood mood);
}
