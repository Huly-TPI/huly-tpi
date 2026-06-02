package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.StreamChatUseCase;
import com.huly.backend.presentation.dto.chat.ChatHistoryPageResponse;
import com.huly.backend.presentation.dto.chat.ChatMessageResponse;
import com.huly.backend.presentation.dto.chat.ChatRequest;
import com.huly.backend.presentation.dto.chat.ChatResponse;
import com.huly.backend.presentation.dto.chat.ChatStreamEventResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Locale;

/**
 * Controlador del chatbot de bienestar emocional Huly.
 * Expone tres operaciones principales:
 *  - POST /api/chat → respuesta bloqueante (request/response normal)
 *  - POST /api/chat/stream → respuesta en streaming via Server-Sent Events (SSE)
 *  - GET /api/chat/{conversationId}/messages → historial paginado de mensajes
 *
 * El streaming usa Reactor (Flux) para enviar cada fragmento de respuesta
 * conforme lo genera la IA, lo que da sensación de respuesta en tiempo real.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final ListChatHistoryUseCase listChatHistoryUseCase;
    private final StreamChatUseCase streamChatUseCase;

    /**
     * Envía un mensaje al chatbot y espera la respuesta completa.
     * Incluye análisis emocional, detección de riesgo y posible reto generado.
     * NOTA: userId está hardcodeado a 1L — pendiente integrar con autenticación real.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        Long userId = 1L; // TODO: extraer userId del token JWT autenticado
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId(), userId);
        return ResponseEntity.ok(toResponse(reply));
    }

    /**
     * Envía un mensaje y devuelve la respuesta en tiempo real via SSE.
     * El cliente recibe eventos del tipo "delta" (fragmentos de texto) y al final
     * un evento "metadata" con el análisis emocional y "done" para señalizar el fin.
     * Produce: text/event-stream (protocolo Server-Sent Events).
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEventResponse>> stream(@RequestBody ChatRequest request) {
        if (request == null || isBlank(request.message()) || isBlank(request.conversationId())) {
            // Valida manualmente porque @Valid no funciona bien con Flux reactivo
            return Flux.just(toServerSentEvent(ChatStreamEvent.error("message y conversationId son obligatorios.")));
        }

        Long userId = 1L; // TODO: extraer userId del token JWT autenticado
        return streamChatUseCase.execute(request.message(), request.conversationId(), userId)
                .map(this::toServerSentEvent);
    }

    /**
     * Devuelve el historial de mensajes de una conversación, paginado.
     * Los mensajes se ordenan por fecha ascendente (más antiguos primero).
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ChatHistoryPageResponse> getHistory(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMessage> result = listChatHistoryUseCase.execute(
                conversationId, PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        return ResponseEntity.ok(toPageResponse(result));
    }

    private Boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Envuelve un ChatStreamEvent en el contenedor SSE con el nombre del tipo de evento. */
    private ServerSentEvent<ChatStreamEventResponse> toServerSentEvent(ChatStreamEvent event) {
        String eventName = event.type().name().toLowerCase(Locale.ROOT);
        return ServerSentEvent.<ChatStreamEventResponse>builder(toStreamResponse(event))
                .event(eventName)
                .build();
    }

    /** Convierte un evento de stream de dominio al DTO de respuesta SSE. */
    private ChatStreamEventResponse toStreamResponse(ChatStreamEvent event) {
        ChatReply reply = event.reply();
        String emotion = reply != null && reply.detectedEmotion() != null ? reply.detectedEmotion().name() : null;
        ChatResponse.Metadata metadata = reply != null && reply.riskDetected() != null
                ? new ChatResponse.Metadata(reply.riskDetected(), reply.matchedWord())
                : null;
        ChatResponse.GeneratedChallenge challenge = reply != null && reply.generatedChallenge() != null
                ? new ChatResponse.GeneratedChallenge(reply.generatedChallenge().title(), reply.generatedChallenge().description())
                : null;

        return new ChatStreamEventResponse(
                event.type().name().toLowerCase(Locale.ROOT),
                event.delta(),
                reply != null ? reply.content() : null,
                emotion,
                reply != null ? reply.intensity() : null,
                metadata,
                challenge,
                event.error()
        );
    }

    /** Convierte la respuesta de dominio del chatbot al DTO de respuesta HTTP. */
    private ChatResponse toResponse(ChatReply reply) {
        String emotion = reply.detectedEmotion() != null ? reply.detectedEmotion().name() : null;
        ChatResponse.Metadata metadata = reply.riskDetected() != null
                ? new ChatResponse.Metadata(reply.riskDetected(), reply.matchedWord())
                : null;
        ChatResponse.GeneratedChallenge challenge = reply.generatedChallenge() != null
                ? new ChatResponse.GeneratedChallenge(reply.generatedChallenge().title(), reply.generatedChallenge().description())
                : null;
        return new ChatResponse(reply.content(), emotion, reply.intensity(), toSuggestedAction(reply.suggestedAction()), challenge, metadata);
    }

    /** Mapea la acción sugerida por el chatbot al DTO correspondiente. Retorna null si no hay acción. */
    private ChatResponse.SuggestedAction toSuggestedAction(SuggestedChatAction action) {
        if (action == null) {
            return null;
        }
        return new ChatResponse.SuggestedAction(
                action.type() != null ? action.type().name() : null,
                action.activityId() != null ? action.activityId().toString() : null,
                action.title(),
                action.description(),
                action.actionUrl(),
                action.emotionalEventId()
        );
    }

    /** Convierte una página de mensajes de dominio al DTO paginado de respuesta. */
    private ChatHistoryPageResponse toPageResponse(Page<ChatMessage> page) {
        List<ChatMessageResponse> content = page.getContent().stream()
                .map(this::toMessageResponse)
                .toList();
        return new ChatHistoryPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.id(),
                msg.role() != null ? msg.role().name() : null,
                msg.content(),
                msg.riskDetected(),
                msg.detectedEmotion() != null ? msg.detectedEmotion().name() : null,
                msg.createdAt()
        );
    }
}
