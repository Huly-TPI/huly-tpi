package com.huly.backend.domain.service.chat;

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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio central del chatbot Huly. Orquesta el flujo completo de una conversación:
 *
 * MODO BLOQUEANTE (processMessage):
 *  1. Construye el contexto: prompt base + memorias vectoriales del usuario + palabras de riesgo + historial
 *  2. Envía el mensaje a la IA y obtiene la respuesta completa
 *  3. Analiza emocionalmente el mensaje y decide si recomendar una actividad
 *  4. Persiste el mensaje del usuario y la respuesta del asistente en BD
 *  5. Indexa el mensaje en la memoria vectorial para contexto futuro
 *
 * MODO STREAMING (streamMessage):
 *  1. Guarda el mensaje del usuario antes de iniciar el stream
 *  2. Recibe la respuesta fragmento a fragmento (delta) y los emite via Flux (SSE)
 *  3. Al finalizar el stream, llama a la IA en modo bloqueante para extraer metadata
 *     emocional (la metadata no puede extraerse inline durante el stream)
 *  4. Emite eventos "metadata" y "done" al cliente con el análisis emocional completo
 *
 * La diferencia entre prompts:
 *  - buildEnrichedPrompt: pide JSON estructurado con emoción + respuesta (modo bloqueante)
 *  - buildStreamingPrompt: pide texto libre natural (el análisis va en segundo llamado)
 *  - buildMetadataPrompt: solo pide el JSON de análisis emocional, sin respuesta conversacional
 */
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

    /** Procesa un mensaje en modo bloqueante y devuelve la respuesta completa enriquecida. */
    public ChatReply processMessage(String message, String conversationId, Long userId) {
        ChatContext context = buildBlockingContext(message, conversationId, userId);

        // Llama a la IA con el prompt enriquecido y el historial de la conversación
        ChatReply reply = llmChatPort.chat(context.systemPrompt(), message, context.history());
        // Analiza emocionalmente y posiblemente agrega una acción sugerida (actividad de bienestar)
        ChatReply finalReply = enrichWithEmotionalRecommendation(message, userId, context, reply);
        saveUserMessage(conversationId, message, finalReply, userId);
        saveAssistantMessage(conversationId, finalReply.content(), userId);
        // Indexa el mensaje en memoria vectorial para que el chatbot lo recuerde en el futuro
        userVectorMemoryService.rememberChatMessage(userId, conversationId, message);

        return finalReply;
    }

    /**
     * Procesa un mensaje en modo streaming, emitiendo eventos reactivos.
     * Flux.defer garantiza que el contexto se construya una vez por suscripción (lazy evaluation).
     * onErrorResume captura cualquier fallo y emite un evento de error en lugar de romper el stream.
     */
    public Flux<ChatStreamEvent> streamMessage(String message, String conversationId, Long userId) {
        return Flux.defer(() -> {
            ChatContext context = buildStreamingContext(message, conversationId, userId);
            // Guarda el mensaje del usuario antes de iniciar el stream (sin metadata emocional aún)
            saveUserMessage(conversationId, message, ChatReply.of(""), userId);

            // Acumula los fragmentos de respuesta del asistente para guardarlos al final
            StringBuilder assistantContent = new StringBuilder();

            return streamingLLMChatPort.stream(context.systemPrompt(), message, context.history())
                    .filter(delta -> delta != null && !delta.isBlank())
                    .map(delta -> {
                        assistantContent.append(delta); // Acumula para guardar completo al terminar
                        return ChatStreamEvent.delta(delta); // Emite cada fragmento al cliente
                    })
                    // Al terminar el stream de texto, extrae metadata y emite eventos finales
                    .concatWith(Mono.fromCallable(() -> completeStream(
                                    message,
                                    conversationId,
                                    userId,
                                    context,
                                    assistantContent.toString()
                            ))
                            .flatMapMany(reply -> Flux.just(
                                    ChatStreamEvent.metadata(reply), // Análisis emocional
                                    ChatStreamEvent.done(reply)      // Señal de fin de stream
                            )))
                    .doOnCancel(() -> log.info("Stream de chat cancelado userId={} conversationId={}",
                            userId, conversationId));
        }).onErrorResume(e -> {
            log.warn("Error durante stream de chat userId={} conversationId={}", userId, conversationId, e);
            return Flux.just(ChatStreamEvent.error(STREAM_ERROR_MESSAGE));
        });
    }

    /**
     * Construye el contexto para modo bloqueante.
     * Usa buildEnrichedPrompt que pide JSON estructurado a la IA (respuesta + metadata en un solo llamado).
     */
    private ChatContext buildBlockingContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        String systemPrompt = promptBuilderService.buildEnrichedPrompt(basePrompt, riskWords, memories);
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId);
        return new ChatContext(basePrompt, systemPrompt, riskWords, memories, history);
    }

    /**
     * Construye el contexto para modo streaming.
     * Usa buildStreamingPrompt que pide texto natural (sin JSON), ya que durante el stream
     * la IA no puede generar JSON válido fragmentado.
     */
    private ChatContext buildStreamingContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        String systemPrompt = promptBuilderService.buildStreamingPrompt(basePrompt, riskWords, memories);
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId);
        return new ChatContext(basePrompt, systemPrompt, riskWords, memories, history);
    }

    /** Obtiene el system prompt activo desde la BD (configurable desde el backoffice). */
    private String basePrompt() {
        return chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");
    }

    /**
     * Finaliza el stream: guarda la respuesta completa del asistente,
     * extrae metadata emocional via un segundo llamado bloqueante a la IA,
     * e indexa el mensaje en memoria vectorial.
     */
    private ChatReply completeStream(
            String message,
            String conversationId,
            Long userId,
            ChatContext context,
            String assistantContent
    ) {
        // Segundo llamado a la IA solo para extraer metadata (emoción, riesgo, etc.)
        ChatReply metadata = extractMetadata(message, context);
        ChatReply finalReply = new ChatReply(
                assistantContent,
                metadata.detectedEmotion(),
                metadata.intensity(),
                metadata.riskDetected(),
                metadata.matchedWord()
        );

        saveAssistantMessage(conversationId, assistantContent, userId);
        userVectorMemoryService.rememberChatMessage(userId, conversationId, message);
        return finalReply;
    }

    /**
     * Extrae metadata emocional del mensaje usando un prompt especializado.
     * Este es el segundo llamado a la IA en el modo streaming (el primero fue el stream de texto).
     * Si falla, retorna una respuesta vacía para no bloquear el flujo.
     */
    private ChatReply extractMetadata(String message, ChatContext context) {
        try {
            String metadataPrompt = promptBuilderService.buildMetadataPrompt(
                    context.basePrompt(),
                    context.riskWords(),
                    context.memories()
            );
            return llmChatPort.chat(metadataPrompt, message, context.history());
        } catch (Exception e) {
            log.warn("No se pudo extraer metadata de chat en streaming", e);
            return ChatReply.of("");
        }
    }

    /**
     * Evalúa si el mensaje amerita una recomendación de actividad de bienestar.
     * Si el análisis emocional lo confirma, persiste el EmotionalEvent y agrega
     * la acción sugerida a la respuesta del chatbot.
     */
    private ChatReply enrichWithEmotionalRecommendation(
            String message,
            Long userId,
            ChatContext context,
            ChatReply reply
    ) {
        ChatRecommendationOutcome outcome = chatEmotionalRecommendationService.evaluate(
                message,
                userId,
                context.basePrompt(),
                context.memories(),
                context.history(),
                reply
        );

        ChatReply enriched = applyAnalysisMetadata(reply, outcome.analysis());
        return outcome.suggestedAction() == null
                ? enriched
                : enriched.withSuggestedAction(outcome.suggestedAction());
    }

    /**
     * Aplica la metadata emocional del análisis a la respuesta del chatbot.
     * Solo aplica si la emoción fue detectada con confianza > 0.
     * Convierte la intensidad de escala [0.0-1.0] a escala [1-10] para el cliente.
     */
    private ChatReply applyAnalysisMetadata(ChatReply reply, EmotionalAnalysisResult analysis) {
        if (analysis == null || analysis.detectedEmotion() == null || analysis.confidence() <= 0.0) {
            return reply;
        }
        return reply.withEmotionalMetadata(analysis.detectedEmotion(), toChatIntensity(analysis.intensity()));
    }

    /** Convierte intensidad de [0.0, 1.0] a escala entera [1, 10] que usa la API. */
    private Integer toChatIntensity(double intensity) {
        return (int) Math.round(Math.max(0.0, Math.min(1.0, intensity)) * 10.0);
    }

    /** Persiste el mensaje del usuario con su metadata emocional. Fallo no crítico: solo loguea. */
    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                    MessageRole.USER,
                    message,
                    reply.detectedEmotion(),
                    reply.riskDetected(),
                    reply.matchedWord()
            ), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    /** Persiste la respuesta del asistente. Fallo no crítico: solo loguea. */
    private void saveAssistantMessage(String conversationId, String content, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, ConversationMessage.of(MessageRole.ASSISTANT, content), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje del asistente userId={} conversationId={}", userId, conversationId, e);
        }
    }

    /** Contiene todos los datos necesarios para procesar un mensaje en la conversación. */
    private record ChatContext(
            String basePrompt,
            String systemPrompt,
            List<RiskWord> riskWords,
            List<VectorMemory> memories,
            List<ConversationMessage> history
    ) {
    }
}
