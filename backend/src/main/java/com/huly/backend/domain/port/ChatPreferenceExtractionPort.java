package com.huly.backend.domain.port;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;

/**
 * Semantic fallback for extracting conversational preferences from free text.
 */
public interface ChatPreferenceExtractionPort {

    ChatPreferenceDetectionResult extract(
            String message,
            ChatPreferenceExpectedField expectedField);
}
