package com.huly.backend.domain.dto.journal;

import com.huly.backend.domain.model.enums.Mood;

import java.time.Instant;

/**
 * Representacion de una entrada de diario dentro de la respuesta de dominio.
 */
public record JournalEntryItem(
        Long id,
        String content,
        Mood mood,
        Instant createdAt
) {
}
