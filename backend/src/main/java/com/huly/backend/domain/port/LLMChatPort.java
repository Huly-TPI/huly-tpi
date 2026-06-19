package com.huly.backend.domain.port;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;

import java.util.List;

public interface LLMChatPort {

    ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history);
}