package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceInitializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RequiredArgsConstructor
public class ListChatHistoryUseCase {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatPreferenceInitializationService chatPreferenceInitializationService;
    private final ChatMapper mapper;

    public ChatHistoryResponse execute(ChatHistoryRequest request) {
        chatPreferenceInitializationService.initialize(request.userId(), request.conversationId());
        int safePage = Math.max(0, request.page());
        int safeSize = Math.max(1, request.size());
        PageRequest pageable = PageRequest.of(
                safePage, safeSize, Sort.by("createdAt").descending());
        Page<ChatMessage> page = chatMessageRepository.findByConversationIdAndUserId(
                request.conversationId(), request.userId(), pageable);
        return toHistoryResponse(page);
    }

    private ChatHistoryResponse toHistoryResponse(Page<ChatMessage> page) {
        List<ChatHistoryResponse.Message> content = page.getContent().stream()
                .map(mapper::toHistoryMessage)
                .toList();
        return new ChatHistoryResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
