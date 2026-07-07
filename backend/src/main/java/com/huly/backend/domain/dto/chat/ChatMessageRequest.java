package com.huly.backend.domain.dto.chat;

/**
 * Request de dominio para procesar un mensaje de chat. Reúne todo lo que el caso de uso
 * necesita, incluido el usuario autenticado (resuelto en la capa de presentación).
 */
public record ChatMessageRequest(Long userId, String conversationId, String message) {
}
