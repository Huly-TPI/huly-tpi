package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.presentation.dto.chat.ChatHistoryPageResponse;
import com.huly.backend.presentation.dto.chat.ChatMessageResponse;
import com.huly.backend.presentation.dto.chat.ChatRequest;
import com.huly.backend.presentation.dto.chat.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final ListChatHistoryUseCase listChatHistoryUseCase;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        Long userId = 1L; // TODO: reemplazar por el usuario autenticado
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId(), userId);
        return ResponseEntity.ok(toResponse(reply));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ChatHistoryPageResponse> getHistory(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMessage> result = listChatHistoryUseCase.execute(
                conversationId, PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        return ResponseEntity.ok(toPageResponse(result));
    }

    private ChatResponse toResponse(ChatReply reply) {
        String emotion = reply.detectedEmotion() != null ? reply.detectedEmotion().name() : null;
        ChatResponse.Metadata metadata = reply.riskDetected() != null
                ? new ChatResponse.Metadata(reply.riskDetected(), reply.matchedWord())
                : null;
        return new ChatResponse(reply.content(), emotion, reply.intensity(), null, null, metadata);
    }

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