package com.huly.backend.domain.repository.journal;

import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;

import java.util.List;

public interface JournalEntryRepository {
    JournalEntry save(Long userId, String content, Mood mood);
    List<JournalEntry> findAllByUserId(Long userId);
}
