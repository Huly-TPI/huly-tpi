package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.chat.ConversationMessage;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StreamingLLMChatPort {

    Flux<String> stream(String systemPrompt, String userMessage, List<ConversationMessage> history);
}
