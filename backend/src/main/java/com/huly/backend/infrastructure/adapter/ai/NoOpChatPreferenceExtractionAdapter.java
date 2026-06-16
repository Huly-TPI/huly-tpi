package com.huly.backend.infrastructure.adapter.ai;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import org.springframework.stereotype.Component;

@Component
public class NoOpChatPreferenceExtractionAdapter implements ChatPreferenceExtractionPort {

    @Override
    public ChatPreferenceDetectionResult extract(
            String message,
            ChatPreferenceExpectedField expectedField) {
        return ChatPreferenceDetectionResult.unrelated();
    }
}
