package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.VectorMemoryService;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.VectorMemoryProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private LLMChatPort llmChatPort;
    @Mock private ChatMemoryPort chatMemoryPort;
    @Mock private ChatConfigRepository chatConfigRepository;
    @Mock private RiskWordRepository riskWordRepository;
    @Mock private PromptBuilderService promptBuilderService;
    @Mock private VectorMemoryService vectorMemoryService;
    @Mock private VectorMemoryProperties vectorMemoryProperties;

    @InjectMocks
    private ChatService chatService;

    @Test
    void processMessage_shouldReturnReplyFromLLM() {
        ChatReply expected = new ChatReply("respuesta", EmotionType.JOY, 8, false, null);
        givenDefaultSetup("prompt base", List.of(), "prompt enriquecido", List.of(), expected);

        ChatReply result = chatService.processMessage("hola", "conv-1", 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void processMessage_shouldUseBasePromptFromConfig() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(new ChatConfig(1L, true, "mi prompt")));
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(eq("mi prompt"), any(), any())).thenReturn("enriquecido");
        when(chatMemoryPort.getHistory(any())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));
        givenVectorMemoryDefaults();

        chatService.processMessage("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(eq("mi prompt"), any(), any());
    }

    @Test
    void processMessage_shouldUseFallbackEmptyPrompt_whenConfigNotFound() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(eq(""), any(), any())).thenReturn("fallback");
        when(chatMemoryPort.getHistory(any())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));
        givenVectorMemoryDefaults();

        chatService.processMessage("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(eq(""), any(), any());
    }

    @Test
    void processMessage_shouldPassActiveRiskWordsToPromptBuilder() {
        RiskWord rw = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(riskWordRepository.findAllActive()).thenReturn(List.of(rw));
        when(promptBuilderService.buildEnrichedPrompt(any(), eq(List.of(rw)), any())).thenReturn("enriquecido");
        when(chatMemoryPort.getHistory(any())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));
        givenVectorMemoryDefaults();

        chatService.processMessage("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(any(), eq(List.of(rw)), any());
    }

    @Test
    void processMessage_shouldFetchHistoryAndPassItToLLM() {
        List<ConversationMessage> history = List.of(ConversationMessage.of(MessageRole.USER, "anterior"));
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any())).thenReturn("prompt");
        when(chatMemoryPort.getHistory("conv-abc")).thenReturn(history);
        when(llmChatPort.chat(any(), any(), eq(history))).thenReturn(ChatReply.of("ok"));
        givenVectorMemoryDefaults();

        chatService.processMessage("msg", "conv-abc", 1L);

        verify(chatMemoryPort).getHistory("conv-abc");
        verify(llmChatPort).chat(any(), eq("msg"), eq(history));
    }

    @Test
    void processMessage_shouldSaveUserMessageWithEmotionAndRiskFromReply() {
        ChatReply reply = new ChatReply("respuesta", EmotionType.SADNESS, 6, true, "suicidio");
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        chatService.processMessage("me siento mal", "conv-1", 5L);

        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq("conv-1"), captor.capture(), eq(5L));

        ConversationMessage userMsg = captor.getAllValues().get(0);
        assertThat(userMsg.role()).isEqualTo(MessageRole.USER);
        assertThat(userMsg.content()).isEqualTo("me siento mal");
        assertThat(userMsg.detectedEmotion()).isEqualTo(EmotionType.SADNESS);
        assertThat(userMsg.riskDetected()).isTrue();
        assertThat(userMsg.matchedWord()).isEqualTo("suicidio");
    }

    @Test
    void processMessage_shouldSaveAssistantMessageWithReplyContent() {
        ChatReply reply = new ChatReply("todo va a estar bien", EmotionType.JOY, 7, false, null);
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        chatService.processMessage("hola", "conv-1", 1L);

        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq("conv-1"), captor.capture(), eq(1L));

        ConversationMessage assistantMsg = captor.getAllValues().get(1);
        assertThat(assistantMsg.role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(assistantMsg.content()).isEqualTo("todo va a estar bien");
        assertThat(assistantMsg.detectedEmotion()).isNull();
        assertThat(assistantMsg.riskDetected()).isNull();
        assertThat(assistantMsg.matchedWord()).isNull();
    }

    @Test
    void processMessage_shouldPassEnrichedPromptToLLM() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any())).thenReturn("prompt final");
        when(chatMemoryPort.getHistory(any())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));
        givenVectorMemoryDefaults();

        chatService.processMessage("msg", "conv-1", 1L);

        verify(llmChatPort).chat(eq("prompt final"), any(), any());
    }

    private void givenDefaultSetup(String basePrompt, List<RiskWord> riskWords,
                                   String enrichedPrompt, List<ConversationMessage> history,
                                   ChatReply reply) {
        when(chatConfigRepository.findFirst()).thenReturn(
                basePrompt.isEmpty() ? Optional.empty()
                        : Optional.of(new ChatConfig(1L, true, basePrompt)));
        when(riskWordRepository.findAllActive()).thenReturn(riskWords);
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any())).thenReturn(enrichedPrompt);
        when(chatMemoryPort.getHistory(any())).thenReturn(history);
        when(llmChatPort.chat(any(), any(), any())).thenReturn(reply);
        givenVectorMemoryDefaults();
    }

    private void givenVectorMemoryDefaults() {
        when(vectorMemoryProperties.getDefaultLimit()).thenReturn(5);
        when(vectorMemoryProperties.getSimilarityThreshold()).thenReturn(0.65);
        when(vectorMemoryService.findRelevantMemories(any(SearchVectorMemoryQuery.class))).thenReturn(List.<VectorMemory>of());
    }
}
