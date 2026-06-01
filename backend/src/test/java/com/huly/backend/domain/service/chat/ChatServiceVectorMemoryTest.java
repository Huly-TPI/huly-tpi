package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.ChatStreamEventType;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.StreamingLLMChatPort;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceVectorMemoryTest {

    @Test
    void streamMessage_shouldEmitDeltasAndSaveAccumulatedAssistantResponse() {
        LLMChatPort llmChatPort = mock(LLMChatPort.class);
        StreamingLLMChatPort streamingLLMChatPort = mock(StreamingLLMChatPort.class);
        FakeChatMemoryPort chatMemoryPort = new FakeChatMemoryPort();
        ChatConfigRepository chatConfigRepository = mock(ChatConfigRepository.class);
        RiskWordRepository riskWordRepository = mock(RiskWordRepository.class);
        PromptBuilderService promptBuilderService = mock(PromptBuilderService.class);
        UserVectorMemoryService userVectorMemoryService = mock(UserVectorMemoryService.class);

        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "hola")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildStreamingPrompt(any(), any(), any())).thenReturn("stream prompt");
        when(promptBuilderService.buildMetadataPrompt(any(), any(), any())).thenReturn("metadata prompt");
        when(streamingLLMChatPort.stream("stream prompt", "hola", List.of()))
                .thenReturn(Flux.just("hola ", "mundo"));
        when(llmChatPort.chat("metadata prompt", "hola", List.of()))
                .thenReturn(new ChatReply("", EmotionType.JOY, 7, false, null));

        ChatService chatService = new ChatService(
                llmChatPort,
                streamingLLMChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                userVectorMemoryService
        );

        List<ChatStreamEvent> events = chatService.streamMessage("hola", "conv-1", 1L).collectList().block();

        assertThat(events).extracting(ChatStreamEvent::type)
                .containsExactly(ChatStreamEventType.DELTA, ChatStreamEventType.DELTA,
                        ChatStreamEventType.METADATA, ChatStreamEventType.DONE);
        assertThat(events.get(2).reply().content()).isEqualTo("hola mundo");
        assertThat(events.get(2).reply().detectedEmotion()).isEqualTo(EmotionType.JOY);
        assertThat(chatMemoryPort.addedMessages).hasSize(2);
        assertThat(chatMemoryPort.addedMessages.get(1).content()).isEqualTo("hola mundo");
        verify(userVectorMemoryService).rememberChatMessage(1L, "conv-1", "hola");
    }

    @Test
    void streamMessage_shouldEmitErrorWhenProviderFails() {
        LLMChatPort llmChatPort = mock(LLMChatPort.class);
        StreamingLLMChatPort streamingLLMChatPort = mock(StreamingLLMChatPort.class);
        FakeChatMemoryPort chatMemoryPort = new FakeChatMemoryPort();
        ChatConfigRepository chatConfigRepository = mock(ChatConfigRepository.class);
        RiskWordRepository riskWordRepository = mock(RiskWordRepository.class);
        PromptBuilderService promptBuilderService = mock(PromptBuilderService.class);
        UserVectorMemoryService userVectorMemoryService = mock(UserVectorMemoryService.class);

        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "hola")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildStreamingPrompt(any(), any(), any())).thenReturn("stream prompt");
        when(streamingLLMChatPort.stream("stream prompt", "hola", List.of()))
                .thenReturn(Flux.error(new RuntimeException("provider down")));

        ChatService chatService = new ChatService(
                llmChatPort,
                streamingLLMChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                userVectorMemoryService
        );

        List<ChatStreamEvent> events = chatService.streamMessage("hola", "conv-1", 1L).collectList().block();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).type()).isEqualTo(ChatStreamEventType.ERROR);
        assertThat(chatMemoryPort.addedMessages).hasSize(1);
    }

    private static final class FakeChatMemoryPort implements ChatMemoryPort {

        private final List<ConversationMessage> addedMessages = new ArrayList<>();

        @Override
        public List<ConversationMessage> getHistory(String conversationId) {
            return List.of();
        }

        @Override
        public void addMessage(String conversationId, ConversationMessage message, Long userId) {
            addedMessages.add(message);
        }
    }
}
