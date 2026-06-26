package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.dto.journal.ListJournalEntriesRequest;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.mapper.journal.ListJournalEntriesMapper;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListJournalEntriesUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final ListJournalEntriesMapper mapper;

    public ListJournalEntriesResponse execute(ListJournalEntriesRequest request) {
        return mapper.toResponse(journalEntryRepository.findAllByUserId(request.userId()));
    }
}
