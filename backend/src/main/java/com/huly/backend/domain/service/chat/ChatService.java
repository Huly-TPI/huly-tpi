package com.huly.backend.domain.service.chat;

import java.util.List;

import org.springframework.stereotype.Service;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatRecommendationOutcome;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.StreamingLLMChatPort;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String STREAM_ERROR_MESSAGE = "No se pudo completar la respuesta del asistente.";

    private final LLMChatPort llmChatPort;
    private final StreamingLLMChatPort streamingLLMChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;
    private final UserVectorMemoryService userVectorMemoryService;
    private final ChatEmotionalRecommendationService chatEmotionalRecommendationService;

    public ChatReply processMessage(String message, String conversationId, Long userId) {
        ChatContext context = buildBlockingContext(message, conversationId, userId);
        ChatRecommendationOutcome recommendationOutcome = chatEmotionalRecommendationService.evaluate(
                message,
                userId,
                context.basePrompt(),
                context.memories(),
                context.history(),
                null);
        var suggestedAction = recommendationOutcome != null ? recommendationOutcome.suggestedAction() : null;
        context = context.withSystemPrompt(promptBuilderService.buildEnrichedPrompt(
                context.basePrompt(),
                context.riskWords(),
                context.memories(),
                suggestedAction));

        ChatReply reply = llmChatPort.chat(context.systemPrompt(), message, context.history());
        ChatReply finalReply = applyRecommendationOutcome(conversationId, userId, reply, recommendationOutcome);
        saveUserMessage(conversationId, message, finalReply, userId);
        saveAssistantMessage(conversationId, finalReply.content(), userId);
        userVectorMemoryService.rememberChatMessage(userId, conversationId, message);

        return finalReply;
    }

    public Flux<ChatStreamEvent> streamMessage(String message, String conversationId, Long userId) {
        return Flux.defer(() -> {
            ChatContext context = buildStreamingContext(message, conversationId, userId);
            saveUserMessage(conversationId, message, ChatReply.of(""), userId);

            StringBuilder assistantContent = new StringBuilder();

            return streamingLLMChatPort.stream(context.systemPrompt(), message, context.history())
                    .filter(delta -> delta != null && !delta.isBlank())
                    .map(delta -> {
                        assistantContent.append(delta);
                        return ChatStreamEvent.delta(delta);
                    })
                    .concatWith(Mono.fromCallable(() -> completeStream(
                            message,
                            conversationId,
                            userId,
                            context,
                            assistantContent.toString()))
                            .flatMapMany(reply -> Flux.just(
                                    ChatStreamEvent.metadata(reply),
                                    ChatStreamEvent.done(reply))))
                    .doOnCancel(() -> log.info("Stream de chat cancelado userId={} conversationId={}",
                            userId, conversationId));
        }).onErrorResume(e -> {
            log.warn("Error durante stream de chat userId={} conversationId={}", userId, conversationId, e);
            return Flux.just(ChatStreamEvent.error(STREAM_ERROR_MESSAGE));
        });
    }

    private ChatContext buildBlockingContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId, userId);
        return new ChatContext(basePrompt, null, riskWords, memories, history);
    }

    private ChatContext buildStreamingContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        String systemPrompt = promptBuilderService.buildStreamingPrompt(basePrompt, riskWords, memories);
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId, userId);
        return new ChatContext(basePrompt, systemPrompt, riskWords, memories, history);
    }

    private String basePrompt() {
        return chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");
    }

    private ChatReply completeStream(
            String message,
            String conversationId,
            Long userId,
            ChatContext context,
            String assistantContent) {
        ChatReply metadata = extractMetadata(message, context);
        ChatReply finalReply = new ChatReply(
                assistantContent,
                metadata.detectedEmotion(),
                metadata.intensity(),
                metadata.riskDetected(),
                metadata.matchedWord());

        saveAssistantMessage(conversationId, assistantContent, userId);
        userVectorMemoryService.rememberChatMessage(userId, conversationId, message);
        return finalReply;
    }

    private ChatReply extractMetadata(String message, ChatContext context) {
        try {
            String metadataPrompt = promptBuilderService.buildMetadataPrompt(
                    context.basePrompt(),
                    context.riskWords(),
                    context.memories());
            return llmChatPort.chat(metadataPrompt, message, context.history());
        } catch (Exception e) {
            log.warn("No se pudo extraer metadata de chat en streaming", e);
            return ChatReply.of("");
        }
    }

    private ChatReply applyRecommendationOutcome(
            String conversationId,
            Long userId,
            ChatReply reply,
            ChatRecommendationOutcome outcome) {
        ChatReply enriched = applyAnalysisMetadata(reply, outcome != null ? outcome.analysis() : null);

        if (outcome == null || outcome.suggestedAction() == null) {
            userVectorMemoryService.rememberGeneratedChallenge(userId, conversationId, enriched.generatedChallenge());
            return enriched;
        }

        userVectorMemoryService.rememberRecommendedActivity(
                userId,
                conversationId,
                outcome.suggestedAction().emotionalEventId(),
                outcome.suggestedAction());
        return new ChatReply(
                enriched.content(),
                enriched.detectedEmotion(),
                enriched.intensity(),
                enriched.riskDetected(),
                enriched.matchedWord(),
                outcome.suggestedAction(),
                null);
    }

    private ChatReply applyAnalysisMetadata(ChatReply reply, EmotionalAnalysisResult analysis) {
        if (analysis == null || analysis.detectedEmotion() == null || analysis.confidence() <= 0.0) {
            return reply;
        }
        return reply.withEmotionalMetadata(analysis.detectedEmotion(), toChatIntensity(analysis.intensity()));
    }

    private Integer toChatIntensity(double intensity) {
        return (int) Math.round(Math.max(0.0, Math.min(1.0, intensity)) * 10.0);
    }

    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                    MessageRole.USER,
                    message,
                    reply.detectedEmotion(),
                    reply.riskDetected(),
                    reply.matchedWord()), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private void saveAssistantMessage(String conversationId, String content, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, ConversationMessage.of(MessageRole.ASSISTANT, content), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje del asistente userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private record ChatContext(
            String basePrompt,
            String systemPrompt,
            List<RiskWord> riskWords,
            List<VectorMemory> memories,
            List<ConversationMessage> history) {
        private ChatContext withSystemPrompt(String nextSystemPrompt) {
            return new ChatContext(basePrompt, nextSystemPrompt, riskWords, memories, history);
        }
    }
}
