package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.UpdateBotConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.BotConfigResponse;
import com.huly.backend.infrastructure.presentation.mapper.chatBotConfig.BotConfigPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/chat/config")
public class BotConfigController {

    private final GetBotConfigUseCase getBotConfigUseCase;
    private final UpdateBotConfigUseCase updateBotConfigUseCase;
    private final BotConfigPresentationMapper botConfigPresentationMapper;



    @GetMapping
    public ResponseEntity<BotConfigResponse> getConfig() {
        GetBotConfigResponse config = getBotConfigUseCase.execute();
        return ResponseEntity.ok(botConfigPresentationMapper.toResponse(config));
    }

    @PutMapping
    public ResponseEntity<BotConfigResponse> updateConfig(@RequestBody @Valid UpdateBotConfigRequest request) {
        UpdateBotConfigResponse updated = updateBotConfigUseCase.execute(
                botConfigPresentationMapper.toUpdateRequest(request));
        return ResponseEntity.ok(botConfigPresentationMapper.toResponse(updated));
    }
}
