package com.huly.backend.domain.mapper.journal;

import com.huly.backend.domain.dto.journal.JournalEntryItem;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.model.journal.JournalEntry;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de entradas de diario.
 */
public class ListJournalEntriesMapper {

    public ListJournalEntriesResponse toResponse(List<JournalEntry> entries) {
        List<JournalEntryItem> items = entries.stream()
                .map(this::toItem)
                .toList();
        return new ListJournalEntriesResponse(items);
    }

    private JournalEntryItem toItem(JournalEntry entry) {
        return new JournalEntryItem(
                entry.getId(),
                entry.getContent(),
                entry.getMood(),
                entry.getCreatedAt()
        );
    }
}
