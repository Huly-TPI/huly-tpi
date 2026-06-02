package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;

import java.util.List;

public interface EmotionalAnalysisPort {

    EmotionalAnalysisResult analyze(String systemPrompt, String userMessage, List<ConversationMessage> history);
}
