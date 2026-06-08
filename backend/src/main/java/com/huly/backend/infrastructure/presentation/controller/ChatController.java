package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.StreamChatUseCase;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatChallengeDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatHistoryPageResponse;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatMessageResponse;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatRequest;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatResponse;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatStreamEventResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final ListChatHistoryUseCase listChatHistoryUseCase;
    private final StreamChatUseCase streamChatUseCase;
    private final UserVectorMemoryService userVectorMemoryService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChatRequest request) {
        Long userId = getUserId(principal);
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId(), userId);
        return ResponseEntity.ok(toResponse(reply));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEventResponse>> stream(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ChatRequest request) {
        if (request == null || isBlank(request.message()) || isBlank(request.conversationId())) {
            return Flux.just(toServerSentEvent(ChatStreamEvent.error("message y conversationId son obligatorios.")));
        }

        Long userId = getUserId(principal);
        return streamChatUseCase.execute(request.message(), request.conversationId(), userId)
                .map(this::toServerSentEvent);
    }

    @PostMapping("/challenge-decision")
    public ResponseEntity<Void> challengeDecision(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChatChallengeDecisionRequest request) {
        Long userId = getUserId(principal);
        userVectorMemoryService.rememberChallengeDecision(
                userId,
                request.conversationId(),
                request.title(),
                request.description(),
                request.decision());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ChatHistoryPageResponse> getHistory(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(principal);
        Page<ChatMessage> result = listChatHistoryUseCase.execute(
                conversationId, userId, PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        return ResponseEntity.ok(toPageResponse(result));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }

    private Boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ServerSentEvent<ChatStreamEventResponse> toServerSentEvent(ChatStreamEvent event) {
        String eventName = event.type().name().toLowerCase(Locale.ROOT);
        return ServerSentEvent.<ChatStreamEventResponse>builder(toStreamResponse(event))
                .event(eventName)
                .build();
    }

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
