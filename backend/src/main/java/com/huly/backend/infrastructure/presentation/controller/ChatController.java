package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.service.chat.ChatQuotaService;
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
    private final ChatQuotaService chatQuotaService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChatRequest request) {
        Long userId = getUserId(principal);
        ChatReplyResponse reply = chatUseCase.execute(
                new ChatMessageRequest(userId, request.conversationId(), request.message()));
        ChatQuotaService.RemainingQuota quota = chatQuotaService.getRemainingQuota(userId);
        return ResponseEntity.ok(toResponse(reply, quota, null, null));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> sendAudioMessage(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("audio") MultipartFile audio,
            @RequestParam("conversationId") String conversationId) {
        Long userId = getUserId(principal);
        chatQuotaService.assertWithinAudioLimit(userId);
        ChatReplyResponse reply = audioChatUseCase.execute(audio, conversationId, userId);
        ChatQuotaService.RemainingQuota quota = chatQuotaService.getRemainingQuota(userId);
        ChatQuotaService.RemainingAudioQuota audioQuota = chatQuotaService.getRemainingAudioQuota(userId);
        return ResponseEntity.ok(toResponse(reply, quota, audioQuota.remaining(), audioQuota.limitMessage()));
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
        ChatHistoryResponse result = listChatHistoryUseCase.execute(
                new ChatHistoryRequest(userId, conversationId, page, size));
        return ResponseEntity.ok(toPageResponse(result));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }

    private ChatResponse toResponse(ChatReplyResponse reply, ChatQuotaService.RemainingQuota quota,
                                    Integer remainingAudio, String audioLimitMessage) {
        String emotion = reply.detectedEmotion() != null ? reply.detectedEmotion().name() : null;
        ChatResponse.Metadata metadata = reply.riskDetected() != null
                ? new ChatResponse.Metadata(reply.riskDetected(), reply.matchedWord())
                : null;
        ChatResponse.GeneratedChallenge challenge = reply.generatedChallenge() != null
                ? new ChatResponse.GeneratedChallenge(reply.generatedChallenge().title(), reply.generatedChallenge().description())
                : null;
        return new ChatResponse(reply.content(), emotion, reply.intensity(), toSuggestedAction(reply.suggestedAction()),
                challenge, metadata, quota.remaining(), quota.limitMessage(), remainingAudio, audioLimitMessage);
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

    private ChatHistoryPageResponse toPageResponse(ChatHistoryResponse page) {
        List<ChatMessageResponse> content = page.content().stream()
                .map(this::toMessageResponse)
                .toList();
        return new ChatHistoryPageResponse(
                content,
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatHistoryResponse.Message msg) {
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
