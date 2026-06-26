package com.huly.backend.domain.dto.journal;

import com.huly.backend.domain.model.enums.Mood;

import java.time.Instant;

/**
 * Respuesta de dominio luego de crear una entrada de diario emocional.
 */
public record CreateJournalEntryResponse(
        Long id,
        String content,
        Mood mood,
        Instant createdAt
) {
}
