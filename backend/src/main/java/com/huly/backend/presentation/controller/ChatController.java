package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.ChatReply;
import com.huly.backend.domain.useCase.ChatUseCase;
import com.huly.backend.presentation.dto.ChatRequest;
import com.huly.backend.presentation.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId());
        return ResponseEntity.ok(new ChatResponse(reply.content(), null, null, null, null, null));
    }
}