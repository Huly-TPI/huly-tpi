package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.ChatConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.huly.backend.domain.service.Chatbot.BotConfigService;


@RestController
@RequestMapping("/api/admin/chat/config")
public class BotConfigController {
    
    private final BotConfigService botConfigService;

    public BotConfigController (BotConfigService botConfigService){
        this.botConfigService = botConfigService;
    }

    @GetMapping
    public ResponseEntity<ChatConfig> getConfig(){
        return ResponseEntity.ok(botConfigService.getConfig());
    }

    @PutMapping
    public ResponseEntity<ChatConfig> updateConfig(@RequestBody ChatConfig chatConfig){
        return ResponseEntity.ok(botConfigService.updateConfig(chatConfig));
    }


}
