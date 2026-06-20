package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.UpdateBotConfigCommand;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.UpdateBotConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.BotConfigResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/chat/config")
public class BotConfigController {

    private final GetBotConfigUseCase getBotConfigUseCase;
    private final UpdateBotConfigUseCase updateBotConfigUseCase;

    public BotConfigController(GetBotConfigUseCase getBotConfigUseCase,
                               UpdateBotConfigUseCase updateBotConfigUseCase) {
        this.getBotConfigUseCase = getBotConfigUseCase;
        this.updateBotConfigUseCase = updateBotConfigUseCase;
    }

    @GetMapping
    public ResponseEntity<BotConfigResponse> getConfig() {
        ChatConfig config = getBotConfigUseCase.execute();
        return ResponseEntity.ok(toResponse(config));
    }

    @PutMapping
    public ResponseEntity<BotConfigResponse> updateConfig(@RequestBody @Valid UpdateBotConfigRequest request) {
        UpdateBotConfigCommand command = new UpdateBotConfigCommand(
                request.riskDetectionEnabled(),
                request.systemPrompt(),
                request.preferredNameQuestionEnabled(),
                request.communicationStyleQuestionEnabled()
        );
        ChatConfig updated = updateBotConfigUseCase.execute(command);
        return ResponseEntity.ok(toResponse(updated));
    }

    private BotConfigResponse toResponse(ChatConfig config) {
        return new BotConfigResponse(
                config.getId(),
                config.getRiskDetectionEnabled(),
                config.getSystemPrompt(),
                config.getPreferredNameQuestionEnabled(),
                config.getCommunicationStyleQuestionEnabled()
        );
    }
}
