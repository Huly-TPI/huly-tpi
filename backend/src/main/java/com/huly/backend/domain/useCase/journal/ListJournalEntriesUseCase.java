package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListJournalEntriesUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public List<JournalEntry> execute(Long userId) {
        return journalEntryRepository.findAllByUserId(userId);
    }
}
