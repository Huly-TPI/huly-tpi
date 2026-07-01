package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.chat.*;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.port.LLMChatPort;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseTest {

    @Mock private LLMChatPort llmChatPort;
    @Mock private ChatMemoryPort chatMemoryPort;
    @Mock private ChatConfigRepository chatConfigRepository;
    @Mock private RiskWordRepository riskWordRepository;
    @Mock private PromptBuilderService promptBuilderService;
    @Mock private UserVectorMemoryService userVectorMemoryService;
    @Mock private GetChatEmotionalRecommendationUseCase getChatEmotionalRecommendationUseCase;
    @Mock private ChatQuotaService chatQuotaService;
    @Mock private UserRepository userRepository;
    @Mock private ChatConversationPreferenceRepository chatConversationPreferenceRepository;
    @Mock private HandleChatPreferencesUseCase handleChatPreferencesUseCase;
    @Spy private ChatMapper mapper = new ChatMapper();

    @InjectMocks
    private ChatUseCase chatUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(handleChatPreferencesUseCase.execute(anyLong(), anyString(), anyString()))
                .thenReturn(ChatPreferenceHandlingResult.continueChat());
        lenient().when(getChatEmotionalRecommendationUseCase.execute(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(ChatRecommendationOutcome.none(EmotionalAnalysisResult.neutral()));
        lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(chatConversationPreferenceRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void execute_shouldReturnPreferenceReplyWithoutCallingLlm_whenPreferenceWasHandled() {
        ChatReply expected = ChatReply.of("Listo, te voy a decir Checho.");
        when(handleChatPreferencesUseCase.execute(42L, "conv-1", "decime Checho"))
                .thenReturn(ChatPreferenceHandlingResult.handled(expected));

        ChatReply result = chatUseCase.execute("decime Checho", "conv-1", 42L);

        assertThat(result).isEqualTo(expected);
        verify(handleChatPreferencesUseCase).execute(42L, "conv-1", "decime Checho");
        verifyNoInteractions(llmChatPort);
    }

    @Test
    void processMessage_shouldReturnReplyFromLLM() {
        ChatReply expected = new ChatReply("respuesta", EmotionType.JOY, 8, false, null);
        givenDefaultSetup("prompt base", List.of(), "prompt enriquecido", List.of(), expected);

        ChatReply result = chatUseCase.execute("hola", "conv-1", 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void processMessage_shouldUseBasePromptFromConfig() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(new ChatConfig(1L, true, "mi prompt")));
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(eq("mi prompt"), any(), any(), any(), any(), any())).thenReturn("enriquecido");
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(eq("mi prompt"), any(), any(), any(), any(), any());
    }

    @Test
    void processMessage_shouldUseFallbackEmptyPrompt_whenConfigNotFound() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(eq(""), any(), any(), any(), any(), any())).thenReturn("fallback");
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(eq(""), any(), any(), any(), any(), any());
    }

    @Test
    void processMessage_shouldPassRegisteredNameAndPreferencesToPromptBuilder() {
        AppUser user = AppUser.builder().id(1L).name("Sergio Ramírez").build();
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Checho")
                .communicationStyle(CommunicationStyle.DIRECT)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatConversationPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(chatMemoryPort.getHistory("conv-1", 1L)).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any(), any(), any(), any()))
                .thenReturn("personalizado");
        when(llmChatPort.chat("personalizado", "msg", List.of())).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-1", 1L);

        ArgumentCaptor<ChatPersonalizationContext> personalizationCaptor =
                ArgumentCaptor.forClass(ChatPersonalizationContext.class);
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), any(), any(), personalizationCaptor.capture());
        assertThat(personalizationCaptor.getValue().registeredName()).isEqualTo("Sergio Ramírez");
        assertThat(personalizationCaptor.getValue().preferredName()).isEqualTo("Checho");
        assertThat(personalizationCaptor.getValue().communicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
    }

    @Test
    void processMessage_shouldPassActiveRiskWordsToPromptBuilder() {
        RiskWord rw = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of(rw));
        when(promptBuilderService.buildEnrichedPrompt(any(), eq(List.of(rw)), any(), any(), any(), any())).thenReturn("enriquecido");
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-1", 1L);

        verify(promptBuilderService).buildEnrichedPrompt(any(), eq(List.of(rw)), any(), any(), any(), any());
    }

    @Test
    void processMessage_shouldFetchHistoryAndPassItToLLM() {
        List<ConversationMessage> history = List.of(ConversationMessage.of(MessageRole.USER, "anterior"));
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any(), any(), any(), any())).thenReturn("prompt");
        when(chatMemoryPort.getHistory("conv-abc", 1L)).thenReturn(history);
        when(llmChatPort.chat(any(), any(), eq(history))).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-abc", 1L);

        verify(chatMemoryPort).getHistory("conv-abc", 1L);
        verify(llmChatPort).chat(any(), eq("msg"), eq(history));
    }

    @Test
    void processMessage_shouldSaveUserMessageWithEmotionAndRiskFromReply() {
        ChatReply reply = new ChatReply("respuesta", EmotionType.SADNESS, 6, true, "suicidio");
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        chatUseCase.execute("me siento mal", "conv-1", 5L);

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

        chatUseCase.execute("hola", "conv-1", 1L);

        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq("conv-1"), captor.capture(), eq(1L));

        ConversationMessage assistantMsg = captor.getAllValues().get(1);
        assertThat(assistantMsg.role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(assistantMsg.content()).isEqualTo("todo va a estar bien");
    }

    @Test
    void processMessage_shouldUseReusableUserMemoryService() {
        VectorMemory memory = new VectorMemory("mem-1", 1L, null, null, "recuerdo", null, 0.9);
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(1L, "msg")).thenReturn(List.of(memory));
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), eq(List.of(memory)), any(), any(), any())).thenReturn("prompt final");
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        chatUseCase.execute("msg", "conv-1", 1L);

        verify(userVectorMemoryService).findRelevantUserMemories(1L, "msg");
        
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
        assertThat(captor.getValue().content()).isEqualTo("msg");
        
        verify(llmChatPort).chat(eq("prompt final"), any(), any());
    }

    @Test
    void processMessage_shouldAttachSuggestedActionFromEmotionalRecommendation() {
        ChatReply reply = new ChatReply("te acompaño", EmotionType.SADNESS, 8, false, null);
        SuggestedChatAction action = new SuggestedChatAction(
                ActivityType.DIARY,
                2L,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                "/api/activities",
                30L
        );
        EmotionalAnalysisResult analysis = new EmotionalAnalysisResult(
                true,
                EmotionType.SADNESS,
                0.9,
                -0.8,
                0.2,
                -0.7,
                0.85,
                "procesar tristeza",
                "malestar claro"
        );
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);
        when(getChatEmotionalRecommendationUseCase.execute(any(), any(), any(), any(), any(), any(), eq(false)))
                .thenReturn(new ChatRecommendationOutcome(analysis, action));

        ChatReply result = chatUseCase.execute("estoy decaido", "conv-1", 1L);

        assertThat(result.suggestedAction()).isEqualTo(action);
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.SADNESS);
        assertThat(result.intensity()).isEqualTo(9);
    }

    @Test
    void processMessage_shouldForceActivityRecommendation_whenUserExplicitlyRequestsActivity() {
        ChatReply reply = new ChatReply("te recomiendo escribir", EmotionType.CALM, 4, false, null);
        SuggestedChatAction action = new SuggestedChatAction(
                ActivityType.DIARY,
                2L,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                "/api/activities",
                30L
        );
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);
        when(getChatEmotionalRecommendationUseCase.execute(any(), any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(new ChatRecommendationOutcome(EmotionalAnalysisResult.neutral(), action));

        ChatReply result = chatUseCase.execute("dame una recomendacion de actividad", "conv-1", 1L);

        assertThat(result.suggestedAction()).isEqualTo(action);
        verify(getChatEmotionalRecommendationUseCase).execute(any(), any(), any(), any(), any(), any(), eq(true));
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), eq(action), eq(ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST), any());
    }

    @Test
    void processMessage_shouldForceChallengePrompt_whenUserExplicitlyRequestsChallenge() {
        ChatReply reply = new ChatReply("claro", EmotionType.MOTIVATION, 5, false, null);
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        ChatReply result = chatUseCase.execute("quiero un reto", "conv-1", 1L);

        assertThat(result.generatedChallenge()).isNotNull();
        assertThat(result.generatedChallenge().title()).isEqualTo("Reto de accion pequena");
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), eq(null), eq(ChatUserIntent.CHALLENGE_REQUEST), any());
    }

    @Test
    void processMessage_shouldAppendStyleQuestionWhenReplyIsSafe() {
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Crack")
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .build();
        when(chatConversationPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(preference));
        when(handleChatPreferencesUseCase.execute(1L, "conv-1", "qué onda"))
                .thenReturn(ChatPreferenceHandlingResult.continueChatAndOfferStyle());
        givenDefaultSetup(
                "",
                List.of(),
                "prompt",
                List.of(),
                new ChatReply("Todo bien por acá.", EmotionType.JOY, 3, false, null));

        ChatReply result = chatUseCase.execute("qué onda", "conv-1", 1L);

        assertThat(result.content())
                .contains("Todo bien por acá.")
                .contains("Cómo te gustaría que te hable");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(chatConversationPreferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    void processMessage_shouldPostponeStyleQuestionWhenRiskIsDetected() {
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Crack")
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .build();
        when(chatConversationPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(preference));
        when(handleChatPreferencesUseCase.execute(1L, "conv-1", "estoy muy mal"))
                .thenReturn(ChatPreferenceHandlingResult.continueChatAndOfferStyle());
        givenDefaultSetup(
                "",
                List.of(),
                "prompt",
                List.of(),
                new ChatReply("Estoy acá para acompañarte.", EmotionType.SADNESS, 9, true, "riesgo"));

        ChatReply result = chatUseCase.execute("estoy muy mal", "conv-1", 1L);

        assertThat(result.content()).doesNotContain("Cómo te gustaría");
        verify(chatConversationPreferenceRepository, never()).save(any());
    }

    @Test
    void processMessage_shouldNullifyChallenge_whenUserAcceptsChallenge() {
        ChatReply.GeneratedChallenge generated = new ChatReply.GeneratedChallenge("Reto", "Haz algo");
        ChatReply reply = new ChatReply("¡Qué bueno!", EmotionType.JOY, 5, false, null, null, generated);
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        ChatReply result = chatUseCase.execute("Acepto este reto", "conv-1", 1L);

        assertThat(result.generatedChallenge()).isNull();
    }

    @Test
    void processMessage_shouldNullifyChallenge_whenUserRejectsChallenge() {
        ChatReply.GeneratedChallenge generated = new ChatReply.GeneratedChallenge("Reto", "Haz algo");
        ChatReply reply = new ChatReply("No hay problema", EmotionType.JOY, 5, false, null, null, generated);
        givenDefaultSetup("", List.of(), "prompt", List.of(), reply);

        ChatReply result = chatUseCase.execute("Rechazo este reto por ahora", "conv-1", 1L);

        assertThat(result.generatedChallenge()).isNull();
    }

    private void givenDefaultSetup(String basePrompt, List<RiskWord> riskWords,
                                   String enrichedPrompt, List<ConversationMessage> history,
                                   ChatReply reply) {
        when(chatConfigRepository.findFirst()).thenReturn(
                basePrompt.isEmpty() ? Optional.empty()
                        : Optional.of(new ChatConfig(1L, true, basePrompt)));
        when(userVectorMemoryService.findRelevantUserMemories(any(), any())).thenReturn(List.of());
        when(riskWordRepository.findAllActive()).thenReturn(riskWords);
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), any(), any(), any(), any())).thenReturn(enrichedPrompt);
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(history);
        when(llmChatPort.chat(any(), any(), any())).thenReturn(reply);
    }
}
