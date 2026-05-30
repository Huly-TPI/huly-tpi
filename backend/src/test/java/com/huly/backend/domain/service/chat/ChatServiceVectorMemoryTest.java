package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.VectorMemoryService;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.VectorMemoryProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceVectorMemoryTest {

    private final VectorMemoryProperties vectorMemoryProperties = new VectorMemoryProperties();
    private final PromptBuilderService promptBuilderService = new PromptBuilderService();

    @Test
    void processMessage_shouldSearchRelevantMemoriesAndPassThemToAnthropic() {
        FakeVectorMemoryService vectorMemoryService = new FakeVectorMemoryService(List.of(
                new VectorMemory("mem-1", 1L, VectorMemorySource.CHATBOT, "conv-1",
                        "Al usuario le gusta la play", Map.of("userId", "1"), 0.92)
        ));
        FakeLLMChatPort llmChatPort = new FakeLLMChatPort(ChatReply.of("respuesta"));
        FakeChatMemoryPort chatMemoryPort = new FakeChatMemoryPort();
        FakeChatConfigRepository chatConfigRepository = new FakeChatConfigRepository(Optional.of(
                new ChatConfig(1L, true, "Prompt base")
        ));
        FakeRiskWordRepository riskWordRepository = new FakeRiskWordRepository(List.of());

        ChatService chatService = new ChatService(
                llmChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                vectorMemoryService,
                vectorMemoryProperties
        );

        ChatReply reply = chatService.processMessage("me gusta la play", "conv-1", 1L);

        assertThat(reply.content()).isEqualTo("respuesta");
        assertThat(vectorMemoryService.lastSearchQuery).isNotNull();
        assertThat(vectorMemoryService.lastSearchQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryService.lastSearchQuery.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(llmChatPort.lastSystemPrompt).contains("Al usuario le gusta la play");
        assertThat(chatMemoryPort.addedMessages).hasSize(2);
        assertThat(chatMemoryPort.addedMessages.get(0).role()).isEqualTo(MessageRole.USER);
        assertThat(chatMemoryPort.addedMessages.get(1).role()).isEqualTo(MessageRole.ASSISTANT);
    }

    @Test
    void processMessage_shouldSaveVectorMemoryAfterReply() {
        FakeVectorMemoryService vectorMemoryService = new FakeVectorMemoryService(List.of());
        FakeLLMChatPort llmChatPort = new FakeLLMChatPort(ChatReply.of("respuesta"));
        FakeChatMemoryPort chatMemoryPort = new FakeChatMemoryPort();
        FakeChatConfigRepository chatConfigRepository = new FakeChatConfigRepository(Optional.empty());
        FakeRiskWordRepository riskWordRepository = new FakeRiskWordRepository(List.of());

        ChatService chatService = new ChatService(
                llmChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                vectorMemoryService,
                vectorMemoryProperties
        );

        chatService.processMessage("me gusta jugar a la play", "conv-99", 1L);

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand command = vectorMemoryService.savedCommands.get(0);
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.source()).isEqualTo("USER_CHAT_MESSAGE");
        assertThat(command.contentType()).isEqualTo("CHAT_MESSAGE");
        assertThat(command.content()).isEqualTo("me gusta jugar a la play");
        assertThat(command.conversationId()).isEqualTo("conv-99");
        assertThat(command.sourceId()).isEqualTo("conv-99");
    }

    private static final class FakeVectorMemoryService implements VectorMemoryService {

        private final List<VectorMemory> memoriesToReturn;
        private final List<SaveVectorMemoryCommand> savedCommands = new ArrayList<>();
        private SearchVectorMemoryQuery lastSearchQuery;

        private FakeVectorMemoryService(List<VectorMemory> memoriesToReturn) {
            this.memoriesToReturn = memoriesToReturn;
        }

        @Override
        public void saveMemory(SaveVectorMemoryCommand command) {
            savedCommands.add(command);
        }

        @Override
        public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
            lastSearchQuery = query;
            return memoriesToReturn;
        }

        @Override
        public void deleteMemories(com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand command) {
            // No se usa en este test.
        }
    }

    private static final class FakeLLMChatPort implements LLMChatPort {

        private final ChatReply reply;
        private String lastSystemPrompt;
        private String lastUserMessage;
        private List<ConversationMessage> lastHistory;

        private FakeLLMChatPort(ChatReply reply) {
            this.reply = reply;
        }

        @Override
        public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
            this.lastSystemPrompt = systemPrompt;
            this.lastUserMessage = userMessage;
            this.lastHistory = history;
            return reply;
        }
    }

    private static final class FakeChatMemoryPort implements ChatMemoryPort {

        private final List<ConversationMessage> history = List.of();
        private final List<ConversationMessage> addedMessages = new ArrayList<>();

        @Override
        public List<ConversationMessage> getHistory(String conversationId) {
            return history;
        }

        @Override
        public void addMessage(String conversationId, ConversationMessage message, Long userId) {
            addedMessages.add(message);
        }
    }

    private static final class FakeChatConfigRepository implements ChatConfigRepository {

        private final Optional<ChatConfig> config;

        private FakeChatConfigRepository(Optional<ChatConfig> config) {
            this.config = config;
        }

        @Override
        public Optional<ChatConfig> findById(Long id) {
            return config;
        }

        @Override
        public ChatConfig save(ChatConfig chatConfig) {
            return chatConfig;
        }

        @Override
        public Optional<ChatConfig> findFirst() {
            return config;
        }
    }

    private static final class FakeRiskWordRepository implements RiskWordRepository {

        private final List<RiskWord> riskWords;

        private FakeRiskWordRepository(List<RiskWord> riskWords) {
            this.riskWords = riskWords;
        }

        @Override
        public RiskWord save(RiskWord riskWord) {
            return riskWord;
        }

        @Override
        public Optional<RiskWord> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public void deleteById(Long id) {
            // No se usa en este test.
        }

        @Override
        public boolean existsById(Long id) {
            return false;
        }

        @Override
        public boolean existsByWordIgnoreCase(String word) {
            return false;
        }

        @Override
        public boolean existsByWordIgnoreCaseAndIdNot(String word, Long id) {
            return false;
        }

        @Override
        public org.springframework.data.domain.Page<RiskWord> findAll(String word, Boolean active, String severity,
                org.springframework.data.domain.Pageable pageable) {
            return org.springframework.data.domain.Page.empty();
        }

        @Override
        public List<RiskWord> findAllActive() {
            return riskWords;
        }
    }
}
