package com.huly.backend.domain.dto.journal;

import java.util.List;

/**
 * Respuesta de dominio con el listado de entradas de diario de un usuario.
 */
public record ListJournalEntriesResponse(List<JournalEntryItem> entries) {
}
