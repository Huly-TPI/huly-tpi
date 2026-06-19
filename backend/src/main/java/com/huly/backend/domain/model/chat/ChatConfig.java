package com.huly.backend.domain.model.chat;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ChatConfig {

    private final Long id;
    private final Boolean riskDetectionEnabled;
    private final String systemPrompt;
    private final Boolean preferredNameQuestionEnabled;
    private final Boolean communicationStyleQuestionEnabled;

    public ChatConfig(Long id, Boolean riskDetectionEnabled, String systemPrompt) {
        this(id, riskDetectionEnabled, systemPrompt, true, true);
    }
}
