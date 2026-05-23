package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.ChatReply;
import com.huly.backend.domain.model.ChatConfig;
import com.huly.backend.domain.model.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.repository.ChatConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatUseCase {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;

    public ChatReply execute(String message, String conversationId) {
        String systemPrompt = chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");

        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId);

        chatMemoryPort.addMessage(conversationId, new ConversationMessage(MessageRole.USER, message));

        ChatReply reply = llmChatPort.chat(systemPrompt, message, history);

        chatMemoryPort.addMessage(conversationId, new ConversationMessage(MessageRole.ASSISTANT, reply.content()));

        return reply;
    }
}