package com.huly.backend.infrastructure.presentation.mapper.journal;

import com.huly.backend.domain.dto.journal.CreateJournalEntryRequest;
import com.huly.backend.domain.dto.journal.CreateJournalEntryResponse;
import com.huly.backend.domain.dto.journal.JournalEntryItem;
import com.huly.backend.domain.dto.journal.ListJournalEntriesRequest;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.infrastructure.presentation.dto.journal.JournalEntryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de diario:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class JournalPresentationMapper {

    public CreateJournalEntryRequest toCreateRequest(Long userId, String content, Mood mood, boolean useTextForAI) {
        return new CreateJournalEntryRequest(userId, content, mood, useTextForAI);
    }

    public ListJournalEntriesRequest toListRequest(Long userId) {
        return new ListJournalEntriesRequest(userId);
    }

    public JournalEntryResponse toJournalEntryResponse(CreateJournalEntryResponse response) {
        return new JournalEntryResponse(
                response.id(),
                response.content(),
                response.mood() != null ? response.mood().name() : null,
                response.createdAt()
        );
    }

    public List<JournalEntryResponse> toJournalEntryResponses(ListJournalEntriesResponse response) {
        return response.entries().stream()
                .map(this::toJournalEntryResponse)
                .toList();
    }

    private JournalEntryResponse toJournalEntryResponse(JournalEntryItem item) {
        return new JournalEntryResponse(
                item.id(),
                item.content(),
                item.mood() != null ? item.mood().name() : null,
                item.createdAt()
        );
    }
}
