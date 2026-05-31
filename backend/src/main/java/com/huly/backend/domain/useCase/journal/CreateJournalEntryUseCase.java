package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntry execute(Long userId, String content, Mood mood) {
        return journalEntryRepository.save(userId, content, mood);
    }
}
