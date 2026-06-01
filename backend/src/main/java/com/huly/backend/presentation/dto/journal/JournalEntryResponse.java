package com.huly.backend.presentation.dto.journal;

import java.time.Instant;

public record JournalEntryResponse(
        Long id,
        String content,
        String mood,
        Instant createdAt
) {}
