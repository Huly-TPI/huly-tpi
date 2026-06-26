package com.huly.backend.domain.dto.journal;

/**
 * Pedido de dominio para listar las entradas de diario de un usuario.
 *
 * @param userId usuario duenio de las entradas.
 */
public record ListJournalEntriesRequest(Long userId) {
}
