package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.useCase.chat.AudioChatUseCase;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.SaveChallengeDecisionUseCase;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatChallengeDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatHistoryPageResponse;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatMessageResponse;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatRequest;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final AudioChatUseCase audioChatUseCase;
    private final ListChatHistoryUseCase listChatHistoryUseCase;
    private final SaveChallengeDecisionUseCase saveChallengeDecisionUseCase;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChatRequest request) {
        Long userId = getUserId(principal);
        ChatReply reply = chatUseCase.execute(request.message(), request.conversationId(), userId);
        return ResponseEntity.ok(toResponse(reply));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> sendAudioMessage(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("audio") MultipartFile audio,
            @RequestParam("conversationId") String conversationId) {
        Long userId = getUserId(principal);
        ChatReply reply = audioChatUseCase.execute(audio, conversationId, userId);
        return ResponseEntity.ok(toResponse(reply));
    }

    @PostMapping("/challenge-decision")
    public ResponseEntity<Void> challengeDecision(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChatChallengeDecisionRequest request) {
        Long userId = getUserId(principal);
        saveChallengeDecisionUseCase.execute(
                userId,
                request.title(),
                request.decision(),
                request.description(),
                request.conversationId()
        );
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
                conversationId, userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(toPageResponse(result));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
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
                msg.createdAt(),
                toSuggestedAction(msg.suggestedAction()),
                msg.generatedChallenge() != null
                        ? new ChatResponse.GeneratedChallenge(msg.generatedChallenge().title(), msg.generatedChallenge().description())
                        : null,
                toFrontendDecision(msg.suggestedActionDecision()),
                toFrontendDecision(msg.challengeDecision())
        );
    }

    private String toFrontendDecision(String decision) {
        if (decision == null || decision.isBlank())
            return null;
        
        return "ACCEPTED".equalsIgnoreCase(decision) ? "accepted" : "rejected";
    }
}
