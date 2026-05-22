package com.huly.backend.presentation.controller;

import com.huly.backend.domain.service.Chatbot.ChatService;
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

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request.message(), request.conversationId()));
    }

}
