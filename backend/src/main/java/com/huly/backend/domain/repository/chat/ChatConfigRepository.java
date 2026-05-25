package com.huly.backend.domain.repository.chat;

import com.huly.backend.domain.model.chat.ChatConfig;

import java.util.Optional;

public interface ChatConfigRepository {

    Optional<ChatConfig> findById(Long id);

    ChatConfig save(ChatConfig chatConfig);

    Optional<ChatConfig> findFirst();
}