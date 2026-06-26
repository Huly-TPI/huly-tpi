package com.huly.backend.domain.mapper.journal;

import com.huly.backend.domain.dto.journal.CreateJournalEntryResponse;
import com.huly.backend.domain.model.journal.JournalEntry;

/**
 * Mapper de dominio para el caso de uso de creacion de entrada de diario.
 */
public class CreateJournalEntryMapper {

    public CreateJournalEntryResponse toResponse(JournalEntry entry) {
        return new CreateJournalEntryResponse(
                entry.getId(),
                entry.getContent(),
                entry.getMood(),
                entry.getCreatedAt()
        );
    }
}
