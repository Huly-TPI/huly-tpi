package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.presentation.dto.chat.ChatRequest;
import com.huly.backend.presentation.dto.chat.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;


    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId());
        return ResponseEntity.ok(toResponse(reply));
    }

    private ChatResponse toResponse(ChatReply reply) {
        ChatResponse.Metadata metadata = reply.riskDetected() != null
                ? new ChatResponse.Metadata(reply.riskDetected(), reply.matchedWord())
                : null;
        return new ChatResponse(reply.content(), reply.detectedEmotion(), reply.intensity(), null, null, metadata);
    }
}