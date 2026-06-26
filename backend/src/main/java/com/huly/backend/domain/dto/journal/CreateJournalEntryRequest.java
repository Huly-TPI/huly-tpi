package com.huly.backend.domain.dto.journal;

import com.huly.backend.domain.model.enums.Mood;

/**
 * Pedido de dominio para crear una entrada de diario emocional.
 *
 * @param userId       usuario que crea la entrada.
 * @param content      contenido de la entrada.
 * @param mood         estado de animo asociado (puede ser nulo).
 * @param useTextForAI indica si el texto se puede usar para enriquecer la memoria de IA.
 */
public record CreateJournalEntryRequest(
        Long userId,
        String content,
        Mood mood,
        boolean useTextForAI
) {
}
