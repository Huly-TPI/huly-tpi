package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.ChatReply;
import com.huly.backend.domain.model.ConversationMessage;

import java.util.List;

public interface LLMChatPort {

    ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history);
}