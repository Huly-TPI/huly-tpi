package com.huly.backend.domain.mapper.chat;

import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper de dominio del chatbot: arma los {@link SaveVectorMemoryCommand} y los
 * {@link ConversationMessage} a partir del estado de dominio, dejando el armado y el
 * formateo de texto fuera del use case.
 */
public class ChatMapper {

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static final String RECOMMENDED_ACTIVITY = "RECOMMENDED_ACTIVITY";
    private static final String GENERATED_CHALLENGE = "GENERATED_CHALLENGE";
    private static final String ACTIVITIES_URL = "/api/activities";

    public SaveVectorMemoryCommand toUserMemoryCommand(Long userId, String conversationId, String message) {
        return new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                userId != null ? userId.toString() : null,
                USER_CHAT_MESSAGE,
                "CHAT_MESSAGE",
                message,
                conversationId,
                null,
                Map.of("createdFrom", CREATED_FROM_USER_MESSAGE, "feature", "CHATBOT"));
    }

    public SaveVectorMemoryCommand toGeneratedChallengeCommand(
            Long userId,
            String conversationId,
            ChatReply.GeneratedChallenge challenge) {
        String description = challenge.description() == null ? "" : challenge.description();
        String content = "Huly sugirio el reto: %s. Descripcion: %s.".formatted(challenge.title(), description);
        String sourceId = String.join(":",
                "generated-challenge",
                conversationId != null && !conversationId.isBlank() ? conversationId.strip() : "unknown",
                challenge.title().strip());
        return new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                sourceId,
                GENERATED_CHALLENGE,
                "GENERATED_CHALLENGE",
                content,
                conversationId,
                null,
                Map.of("createdFrom", CREATED_FROM_USER_MESSAGE, "feature", "CHATBOT_CHALLENGE",
                        "challengeTitle", challenge.title(),
                        "challengeDescription", description));
    }

    public SaveVectorMemoryCommand toRecommendedActivityCommand(
            Long userId,
            String conversationId,
            Long emotionalEventId,
            SuggestedChatAction action) {
        String content = "Huly recomendo la actividad: %s. Tipo: %s. Descripcion: %s.".formatted(
                action.title() == null ? "Actividad" : action.title(),
                action.type() != null ? action.type().name() : "UNKNOWN",
                action.description() == null ? "" : action.description());
        Map<String, Object> extra = new HashMap<>();
        extra.put("createdFrom", CREATED_FROM_USER_MESSAGE);
        extra.put("feature", "CHATBOT_ACTIVITY_RECOMMENDATION");
        extra.put("activityId", action.activityId() == null ? "" : action.activityId().toString());
        extra.put("activityType", action.type() != null ? action.type().name() : "");
        extra.put("emotionalEventId", emotionalEventId != null ? emotionalEventId.toString() : "");
        return new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                emotionalEventId != null ? emotionalEventId.toString() : (userId != null ? userId.toString() : null),
                RECOMMENDED_ACTIVITY,
                "RECOMMENDED_ACTIVITY",
                content,
                conversationId,
                emotionalEventId != null ? emotionalEventId.toString() : null,
                extra);
    }

    public ConversationMessage toUserMessage(String message, ChatReply reply) {
        return new ConversationMessage(
                MessageRole.USER,
                message,
                reply.detectedEmotion(),
                reply.riskDetected(),
                reply.matchedWord(),
                null,
                null,
                null,
                null);
    }

    public ConversationMessage toAssistantMessage(ChatReply reply) {
        return new ConversationMessage(
                MessageRole.ASSISTANT,
                reply.content(),
                null,
                null,
                null,
                reply.suggestedAction(),
                reply.generatedChallenge(),
                null,
                null);
    }

    public CreateEmotionalEventRequest toCreateEmotionalEventRequest(
            String message,
            Long userId,
            EmotionalAnalysisResult analysis,
            EmotionalRecommendationItem recommendation) {
        return new CreateEmotionalEventRequest(
                userId,
                EmotionalEventSource.CHATBOT,
                message,
                analysis.emotionName(),
                analysis.confidence(),
                analysis.valence(),
                analysis.arousal(),
                analysis.dominance(),
                analysis.intensity(),
                analysis.userGoal(),
                generatedRecommendationText(recommendation),
                recommendation.activityId(),
                null);
    }

    public ChatReplyResponse toChatReplyResponse(ChatReply reply) {
        return new ChatReplyResponse(
                reply.content(),
                reply.detectedEmotion(),
                reply.intensity(),
                reply.riskDetected(),
                reply.matchedWord(),
                reply.suggestedAction(),
                reply.generatedChallenge());
    }

    public ChatHistoryResponse.Message toHistoryMessage(ChatMessage message) {
        return new ChatHistoryResponse.Message(
                message.id(),
                message.role(),
                message.content(),
                message.riskDetected(),
                message.detectedEmotion(),
                message.createdAt(),
                message.suggestedAction(),
                message.generatedChallenge(),
                message.suggestedActionDecision(),
                message.challengeDecision());
    }

    public SuggestedChatAction toSuggestedAction(
            EmotionalRecommendationItem recommendation,
            EmotionalEventResponse event) {
        return new SuggestedChatAction(
                recommendation.type(),
                recommendation.activityId(),
                recommendation.title(),
                recommendation.description(),
                ACTIVITIES_URL,
                event.id());
    }

    private String generatedRecommendationText(EmotionalRecommendationItem recommendation) {
        String reason = recommendation.reason() == null || recommendation.reason().isBlank()
                ? ""
                : " " + recommendation.reason();
        return recommendation.title() + ": " + recommendation.description() + reason;
    }
}
