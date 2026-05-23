package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.MessageRole;

public record ConversationMessage(
        MessageRole role,
        String content
) {}