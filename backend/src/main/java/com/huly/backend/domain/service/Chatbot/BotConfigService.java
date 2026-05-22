package com.huly.backend.domain.service.Chatbot;

import com.huly.backend.domain.model.ChatConfig;
import com.huly.backend.domain.repository.ChatConfigRepository;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotConfigService {
    
    private final ChatConfigRepository chatConfigRepository;

    public ChatConfig getConfig(){
        return chatConfigRepository.findFirst()
                .orElse(
                        ChatConfig.builder()
                                .riskDetectionEnabled(false)
                                .systemPrompt("")
                                .build()
                );
    }

    public ChatConfig updateConfig(ChatConfig chatConfig){
        ChatConfig existing = getConfig();
        chatConfig.setId(existing.getId());
        return chatConfigRepository.save(chatConfig);
    }

}
