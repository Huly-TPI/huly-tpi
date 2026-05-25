package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.MessageRole;

public record ConversationMessage(
        MessageRole role,
        String content
) {}