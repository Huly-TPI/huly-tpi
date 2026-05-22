package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.ChatConfig;

import java.util.Optional;

public interface ChatConfigRepository {

    Optional<ChatConfig> findById(Long id);
    ChatConfig save (ChatConfig chatConfig);
    Optional<ChatConfig> findFirst();

}
