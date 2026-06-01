package com.huly.backend.infrastructure.adapter.ai;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoOpEmotionalAnalysisAdapter implements EmotionalAnalysisPort {

    @Override
    public EmotionalAnalysisResult analyze(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        return EmotionalAnalysisResult.neutral();
    }
}
