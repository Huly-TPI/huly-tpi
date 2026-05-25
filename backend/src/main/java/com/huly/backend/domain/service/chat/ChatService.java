package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;

    /**
     * Procesa el mensaje del usuario y retorna la respuesta generada por el asistente.
     *
     * <p>El flujo de ejecución es el siguiente:
     * <ol>
     *   <li>Recupera el prompt base desde la configuración del sistema.</li>
     *   <li>Obtiene las palabras de riesgo activas desde el repositorio.</li>
     *   <li>Construye el prompt enriquecido combinando el base con las instrucciones y palabras de riesgo.</li>
     *   <li>Recupera el historial de mensajes de la conversación actual.</li>
     *   <li>Persiste el mensaje del usuario en la memoria conversacional.</li>
     *   <li>Envía el prompt, el mensaje y el historial al modelo de lenguaje.</li>
     *   <li>Persiste la respuesta del asistente en la memoria conversacional.</li>
     * </ol>
     *
     * @param message        mensaje enviado por el usuario
     * @param conversationId identificador único de la conversación activa
     * @return {@link ChatReply} con la respuesta del asistente, la emoción detectada,
     *         la intensidad y los metadatos de riesgo si corresponde
     */
    public ChatReply processMessage(String message, String conversationId) {
        String basePrompt = chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");

        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        String systemPrompt = promptBuilderService.buildEnrichedPrompt(basePrompt, riskWords);

        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId);

        ChatReply reply = llmChatPort.chat(systemPrompt, message, history);

        chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                MessageRole.USER, message, reply.detectedEmotion(), reply.riskDetected(), reply.matchedWord()
        ));
        chatMemoryPort.addMessage(conversationId, ConversationMessage.of(MessageRole.ASSISTANT, reply.content()));

        return reply;
    }
}
