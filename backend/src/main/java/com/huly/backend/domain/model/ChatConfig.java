package com.huly.backend.domain.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ChatConfig {

    private final Long id;
    private final Boolean riskDetectionEnabled;
    private final String systemPrompt;
}