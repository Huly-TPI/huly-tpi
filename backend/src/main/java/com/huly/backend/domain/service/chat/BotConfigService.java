package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.UpdateBotConfigCommand;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotConfigService {

    private final ChatConfigRepository chatConfigRepository;

    public ChatConfig getConfig() {
        return chatConfigRepository.findFirst()
                .orElse(ChatConfig.builder()
                        .riskDetectionEnabled(false)
                        .systemPrompt("")
                        .preferredNameQuestionEnabled(true)
                        .communicationStyleQuestionEnabled(true)
                        .build());
    }

    public ChatConfig updateConfig(UpdateBotConfigCommand command) {
        ChatConfig existing = getConfig();
        ChatConfig updated = ChatConfig.builder()
                .id(existing.getId())
                .riskDetectionEnabled(
                        command.riskDetectionEnabled() != null
                                ? command.riskDetectionEnabled()
                                : existing.getRiskDetectionEnabled())
                .systemPrompt(
                        command.systemPrompt() != null
                                ? command.systemPrompt()
                                : existing.getSystemPrompt())
                .preferredNameQuestionEnabled(
                        command.preferredNameQuestionEnabled() != null
                                ? command.preferredNameQuestionEnabled()
                                : enabledOrDefault(existing.getPreferredNameQuestionEnabled()))
                .communicationStyleQuestionEnabled(
                        command.communicationStyleQuestionEnabled() != null
                                ? command.communicationStyleQuestionEnabled()
                                : enabledOrDefault(existing.getCommunicationStyleQuestionEnabled()))
                .build();
        return chatConfigRepository.save(updated);
    }

    private Boolean enabledOrDefault(Boolean value) {
        return value == null ? true : value;
    }
}
