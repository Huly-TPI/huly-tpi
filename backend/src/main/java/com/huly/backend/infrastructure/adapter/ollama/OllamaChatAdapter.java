package com.huly.backend.infrastructure.adapter.ollama;

import com.huly.backend.domain.model.ChatReply;
import com.huly.backend.domain.model.ConversationMessage;
import com.huly.backend.domain.provider.LLMChatPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OllamaChatAdapter implements LLMChatPort {

    private final ChatModel chatModel;

    public OllamaChatAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = buildMessages(systemPrompt, userMessage, history);

        Prompt prompt = new Prompt(messages);
        String content = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return new ChatReply(content);
    }

    private List<Message> buildMessages(
            String systemPrompt,
            String userMessage,
            List<ConversationMessage> history
    ) {
        List<Message> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        for (ConversationMessage cm : history) {
            switch (cm.role()) {
                case USER -> messages.add(new UserMessage(cm.content()));
                case ASSISTANT -> messages.add(new AssistantMessage(cm.content()));
            }
        }

        messages.add(new UserMessage(userMessage));
        return messages;
    }
}