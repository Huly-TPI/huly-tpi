package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.UpdateBotConfigCommand;
import com.huly.backend.domain.useCase.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.UpdateBotConfigUseCase;
import com.huly.backend.presentation.dto.BotConfigResponse;
import com.huly.backend.presentation.dto.UpdateBotConfigRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de configuración del chatbot (backoffice/admin).
 * Permite a los administradores ver y modificar:
 *  - systemPrompt: el prompt de sistema que define la personalidad y comportamiento de Huly.
 *  - riskDetectionEnabled: activa/desactiva la detección de palabras de riesgo en las conversaciones.
 *
 * Solo existe una configuración global activa (singleton). Ruta: /api/admin/chat/config
 */
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

    /** Obtiene la configuración actual del chatbot (único registro en BD). */
    @GetMapping
    public ResponseEntity<BotConfigResponse> getConfig() {
        ChatConfig config = getBotConfigUseCase.execute();
        return ResponseEntity.ok(toResponse(config));
    }

    /**
     * Actualiza la configuración global del chatbot.
     * El nuevo systemPrompt entra en vigor inmediatamente en la próxima conversación.
     */
    @PutMapping
    public ResponseEntity<BotConfigResponse> updateConfig(@RequestBody @Valid UpdateBotConfigRequest request) {
        UpdateBotConfigCommand command = new UpdateBotConfigCommand(
                request.riskDetectionEnabled(),
                request.systemPrompt()
        );
        ChatConfig updated = updateBotConfigUseCase.execute(command);
        return ResponseEntity.ok(toResponse(updated));
    }

    private BotConfigResponse toResponse(ChatConfig config) {
        return new BotConfigResponse(
                config.getId(),
                config.getRiskDetectionEnabled(),
                config.getSystemPrompt()
        );
    }
}