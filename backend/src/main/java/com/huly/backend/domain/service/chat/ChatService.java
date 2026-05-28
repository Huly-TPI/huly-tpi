package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.VectorMemoryService;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.VectorMemoryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;
    private final VectorMemoryService vectorMemoryService;
    private final VectorMemoryProperties vectorMemoryProperties;

    /**
     * Procesa el mensaje del usuario y retorna la respuesta generada por el asistente.
     *
     * <p>El flujo de ejecución es el siguiente:
     * <ol>
     *   <li>Recupera el prompt base desde la configuración del sistema.</li>
     *   <li>Busca memorias vectoriales relevantes del usuario para agregar contexto.</li>
     *   <li>Obtiene las palabras de riesgo activas desde el repositorio.</li>
     *   <li>Construye el prompt enriquecido combinando el base con las instrucciones y palabras de riesgo.</li>
     *   <li>Recupera el historial de mensajes de la conversación actual.</li>
     *   <li>Envía el prompt, el mensaje y el historial al modelo de lenguaje.</li>
     *   <li>Persiste el mensaje del usuario en la memoria conversacional.</li>
     *   <li>Persiste la respuesta del asistente en la memoria conversacional.</li>
     *   <li>Guarda el mensaje del usuario como memoria vectorial si contiene información útil.</li>
     * </ol>
     *
     * @param message        mensaje enviado por el usuario
     * @param conversationId identificador único de la conversación activa
     * @return {@link ChatReply} con la respuesta del asistente, la emoción detectada,
     *         la intensidad y los metadatos de riesgo si corresponde
     */
    public ChatReply processMessage(String message, String conversationId, Long userId) {
        String basePrompt = chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");

//        List<VectorMemory> relevantMemories = findRelevantMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
//        String systemPrompt = promptBuilderService.buildEnrichedPrompt(basePrompt, riskWords, relevantMemories);
        String systemPrompt = promptBuilderService.buildEnrichedPrompt(basePrompt, riskWords);

        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId);

        ChatReply reply = llmChatPort.chat(systemPrompt, message, history);

        chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                MessageRole.USER, message, reply.detectedEmotion(), reply.riskDetected(), reply.matchedWord()
        ), userId);
        chatMemoryPort.addMessage(conversationId, ConversationMessage.of(MessageRole.ASSISTANT, reply.content()), userId);
        saveVectorMemory(userId, conversationId, message);

        return reply;
    }

    private List<VectorMemory> findRelevantMemories(Long userId, String message) {
        try {
            return vectorMemoryService.findRelevantMemories(new SearchVectorMemoryQuery(
                    userId,
                    VectorMemorySource.CHATBOT,
                    message,
                    vectorMemoryProperties.getDefaultLimit(),
                    vectorMemoryProperties.getSimilarityThreshold()
            ));
        } catch (Exception e) {
            log.warn("No se pudo recuperar memoria vectorial del chatbot para userId={}", userId, e);
            return List.of();
        }
    }

    private void saveVectorMemory(Long userId, String conversationId, String message) {
        try {
            vectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                    userId,
                    VectorMemorySource.CHATBOT,
                    conversationId,
                    "USER_CHAT_MESSAGE",
                    "CHAT_MESSAGE",
                    message,
                    conversationId,
                    null,
                    Map.of("createdFrom", "USER_MESSAGE")
            ));
        } catch (Exception e) {
            log.warn("No se pudo guardar memoria vectorial del chatbot para userId={}", userId, e);
        }
    }
}
