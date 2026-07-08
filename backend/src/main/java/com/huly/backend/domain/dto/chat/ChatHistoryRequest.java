package com.huly.backend.domain.dto.chat;

/**
 * Request de dominio para listar el historial de una conversación, con la paginación
 * expresada como valores simples (el caso de uso arma el {@code Pageable}).
 */
public record ChatHistoryRequest(Long userId, String conversationId, int page, int size) {
}
