package com.huly.backend.domain.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatConfig {

    private Long id;
    private Boolean riskDetectionEnabled;
    private String systemPrompt;

}
