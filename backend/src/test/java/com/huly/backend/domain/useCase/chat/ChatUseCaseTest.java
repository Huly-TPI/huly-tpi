package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
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
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatPreferenceHandlingService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
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
    @Mock private ChatEmotionalRecommendationService chatEmotionalRecommendationService;
    @Mock private ChatQuotaService chatQuotaService;
    @Mock private UserRepository userRepository;
    @Mock private ChatConversationPreferenceRepository chatConversationPreferenceRepository;
    @Mock private ChatPreferenceHandlingService chatPreferenceHandlingService;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Spy private ChatMapper mapper = new ChatMapper();

    @InjectMocks
    private ChatUseCase chatUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(chatPreferenceHandlingService.handle(anyLong(), anyString(), anyString()))
                .thenReturn(ChatPreferenceHandlingResult.continueChat());
        lenient().when(chatEmotionalRecommendationService.recommend(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(ChatRecommendationOutcome.none(EmotionalAnalysisResult.neutral()));
        lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(chatConversationPreferenceRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        lenient().when(chatMessageRepository.findRecentChallengesByUserId(anyLong(), anyInt()))
                .thenReturn(List.of());
    }

    @Test
    @DisplayName("Devuelve la respuesta de preferencia sin llamar al LLM cuando la preferencia fue atendida")
    void executeShouldReturnPreferenceReplyWithoutCallingLlmWhenPreferenceWasHandled() {
        // --- arrange ---
        givenPreferenceHandled(42L, "conv-1", "decime Checho", ChatReply.of("Listo, te voy a decir Checho."));
        // --- act ---
        ChatReplyResponse result = execute(42L, "conv-1", "decime Checho");
        // --- assert ---
        thenContentIs(result, "Listo, te voy a decir Checho.");
        thenPreferenceServiceWasCalled(42L, "conv-1", "decime Checho");
        thenLlmWasNotCalled();
    }

    @Test
    @DisplayName("Devuelve la respuesta del LLM")
    void processMessageShouldReturnReplyFromLlm() {
        // --- arrange ---
        givenDefaultSetup("prompt base", List.of(), "prompt enriquecido", List.of(),
                new ChatReply("respuesta", EmotionType.JOY, 8, false, null));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "hola");
        // --- assert ---
        thenReplyMatchesEmotion(result, "respuesta", EmotionType.JOY, 8);
    }

    @Test
    @DisplayName("Usa el prompt base de la configuración")
    void processMessageShouldUseBasePromptFromConfig() {
        // --- arrange ---
        givenDefaultSetup("mi prompt", List.of(), "enriquecido", List.of(), ChatReply.of("ok"));
        // --- act ---
        execute(1L, "conv-1", "msg");
        // --- assert ---
        thenPromptBuiltWithBasePrompt("mi prompt");
    }

    @Test
    @DisplayName("Usa un prompt vacío como fallback cuando no hay configuración")
    void processMessageShouldUseFallbackEmptyPromptWhenConfigNotFound() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "fallback", List.of(), ChatReply.of("ok"));
        // --- act ---
        execute(1L, "conv-1", "msg");
        // --- assert ---
        thenPromptBuiltWithBasePrompt("");
    }

    @Test
    @DisplayName("Pasa el nombre registrado y las preferencias al armador de prompt")
    void processMessageShouldPassRegisteredNameAndPreferencesToPromptBuilder() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "personalizado", List.of(), ChatReply.of("ok"));
        givenRegisteredUser(1L, "Sergio Ramírez");
        givenConversationPreference(ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Checho")
                .communicationStyle(CommunicationStyle.DIRECT)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .build());
        // --- act ---
        execute(1L, "conv-1", "msg");
        // --- assert ---
        thenPersonalizationPassedToPromptBuilder("Sergio Ramírez", "Checho", CommunicationStyle.DIRECT);
    }

    @Test
    @DisplayName("Pasa las palabras de riesgo activas al armador de prompt")
    void processMessageShouldPassActiveRiskWordsToPromptBuilder() {
        // --- arrange ---
        givenActiveRiskWord("suicidio", RiskSeverity.HIGH);
        // --- act ---
        execute(1L, "conv-1", "msg");
        // --- assert ---
        thenPromptBuiltWithActiveRiskWord("suicidio", RiskSeverity.HIGH);
    }

    @Test
    @DisplayName("Trae el historial y se lo pasa al LLM")
    void processMessageShouldFetchHistoryAndPassItToLlm() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", conversationHistory(), ChatReply.of("ok"));
        // --- act ---
        execute(1L, "conv-abc", "msg");
        // --- assert ---
        thenHistoryFetchedAndPassedToLlm("conv-abc", 1L, "msg");
    }

    @Test
    @DisplayName("Guarda el mensaje del usuario con la emoción y el riesgo de la respuesta")
    void processMessageShouldSaveUserMessageWithEmotionAndRiskFromReply() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("respuesta", EmotionType.SADNESS, 6, true, "suicidio"));
        // --- act ---
        execute(5L, "conv-1", "me siento mal");
        // --- assert ---
        thenUserMessageSavedWith("conv-1", 5L, "me siento mal", EmotionType.SADNESS, "suicidio");
    }

    @Test
    @DisplayName("Guarda el mensaje del asistente con el contenido de la respuesta")
    void processMessageShouldSaveAssistantMessageWithReplyContent() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("todo va a estar bien", EmotionType.JOY, 7, false, null));
        // --- act ---
        execute(1L, "conv-1", "hola");
        // --- assert ---
        thenAssistantMessageSavedWith("conv-1", 1L, "todo va a estar bien");
    }

    @Test
    @DisplayName("Usa el servicio reutilizable de memoria del usuario")
    void processMessageShouldUseReusableUserMemoryService() {
        // --- arrange ---
        givenSetupWithRelevantMemory(1L, "msg");
        // --- act ---
        execute(1L, "conv-1", "msg");
        // --- assert ---
        thenReusableMemoryServiceUsed(1L, "msg");
    }

    @Test
    @DisplayName("Adjunta la acción sugerida de la recomendación emocional")
    void processMessageShouldAttachSuggestedActionFromEmotionalRecommendation() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("te acompaño", EmotionType.SADNESS, 8, false, null));
        givenEmotionalRecommendation(
                diaryAction(),
                new EmotionalAnalysisResult(true, EmotionType.SADNESS, 0.9, -0.8, 0.2, -0.7, 0.85,
                        "procesar tristeza", "malestar claro"));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "estoy decaido");
        // --- assert ---
        thenSuggestedActionAttached(result);
    }

    @Test
    @DisplayName("Fuerza la recomendación de actividad cuando el usuario la pide explícitamente")
    void processMessageShouldForceActivityRecommendationWhenUserExplicitlyRequestsActivity() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("te recomiendo escribir", EmotionType.CALM, 4, false, null));
        givenForcedActivityRecommendation(diaryAction());
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "dame una recomendacion de actividad");
        // --- assert ---
        thenActivityRecommendationForced(result);
    }

    @Test
    @DisplayName("Fuerza el reto cuando el usuario lo pide explícitamente")
    void processMessageShouldForceChallengePromptWhenUserExplicitlyRequestsChallenge() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("claro", EmotionType.MOTIVATION, 5, false, null));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "quiero un reto");
        // --- assert ---
        thenChallengeForced(result);
    }

    @Test
    @DisplayName("Agrega la pregunta de estilo cuando la respuesta es segura")
    void processMessageShouldAppendStyleQuestionWhenReplyIsSafe() {
        // --- arrange ---
        givenConversationPreference(ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Crack")
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .build());
        givenOfferStyleWhenSafe(1L, "conv-1", "qué onda");
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("Todo bien por acá.", EmotionType.JOY, 3, false, null));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "qué onda");
        // --- assert ---
        thenStyleQuestionAppendedAndMarkedAsked(result);
    }

    @Test
    @DisplayName("Pospone la pregunta de estilo cuando se detecta riesgo")
    void processMessageShouldPostponeStyleQuestionWhenRiskIsDetected() {
        // --- arrange ---
        givenConversationPreference(ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .preferredName("Crack")
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .build());
        givenOfferStyleWhenSafe(1L, "conv-1", "estoy muy mal");
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("Estoy acá para acompañarte.", EmotionType.SADNESS, 9, true, "riesgo"));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "estoy muy mal");
        // --- assert ---
        thenStyleQuestionNotOfferedNorSaved(result);
    }

    @Test
    @DisplayName("Anula el reto cuando el usuario lo acepta")
    void processMessageShouldNullifyChallengeWhenUserAcceptsChallenge() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("¡Qué bueno!", EmotionType.JOY, 5, false, null, null,
                        new ChatReply.GeneratedChallenge("Reto", "Haz algo")));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "Acepto este reto");
        // --- assert ---
        thenChallengeNullified(result);
    }

    @Test
    @DisplayName("Anula el reto cuando el usuario lo rechaza")
    void processMessageShouldNullifyChallengeWhenUserRejectsChallenge() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("No hay problema", EmotionType.JOY, 5, false, null, null,
                        new ChatReply.GeneratedChallenge("Reto", "Haz algo")));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "Rechazo este reto por ahora");
        // --- assert ---
        thenChallengeNullified(result);
    }

    @Test
    @DisplayName("Carga y pasa el historial de retos propuestos al prompt builder")
    void processMessageShouldPassChallengeHistoryToPromptBuilder() {
        // --- arrange ---
        List<ConversationMessage> mockRecentChallenges = List.of(
                new ConversationMessage(MessageRole.ASSISTANT, "Mensaje", null, false, null, null,
                        new ChatReply.GeneratedChallenge("Reto respiracion", "Respira"), null, "REJECTED"),
                new ConversationMessage(MessageRole.ASSISTANT, "Mensaje", null, false, null, null,
                        new ChatReply.GeneratedChallenge("Reto caminar", "Camina"), null, "ACCEPTED")
        );
        when(chatMessageRepository.findRecentChallengesByUserId(anyLong(), anyInt()))
                .thenReturn(mockRecentChallenges);

        givenDefaultSetup("base-prompt", List.of(), "hola", List.of(),
                new ChatReply("Respuesta", EmotionType.NEUTRAL, 5, false, null, null, null));

        // --- act ---
        execute(1L, "conv-1", "hola");

        // --- assert ---
        ArgumentCaptor<ChatPersonalizationContext> captor = ArgumentCaptor.forClass(ChatPersonalizationContext.class);
        verify(promptBuilderService).buildEnrichedPrompt(any(), any(), any(), any(), any(), captor.capture());
        
        List<ChatPersonalizationContext.ChallengeHistoryEntry> history = captor.getValue().challengeHistory();
        assertThat(history).hasSize(2);
        
        ChatPersonalizationContext.ChallengeHistoryEntry respiracion = history.stream()
                .filter(e -> "Reto respiracion".equals(e.title())).findFirst().orElseThrow();
        assertThat(respiracion.rejectedCount()).isEqualTo(1);
        assertThat(respiracion.acceptedCount()).isEqualTo(0);

        ChatPersonalizationContext.ChallengeHistoryEntry caminar = history.stream()
                .filter(e -> "Reto caminar".equals(e.title())).findFirst().orElseThrow();
        assertThat(caminar.rejectedCount()).isEqualTo(0);
        assertThat(caminar.acceptedCount()).isEqualTo(1);
    }

    /*
     * Ramas defensivas INALCANZABLES (no se cubren a propósito):
     *
     * 1) rememberRecommendedActivity -> "if (action == null) return;":
     *    action proviene de outcome.suggestedAction() y el método solo se invoca en la rama else de
     *    applyRecommendationOutcome, es decir cuando outcome != null y outcome.suggestedAction() != null.
     *    Por lo tanto action == null nunca puede darse en ese camino.
     *
     * 2) ensureRequestedChallenge -> sub-condición "suggestedAction != null":
     *    esa sub-condición solo se evalúa cuando el intent es CHALLENGE_REQUEST, y en ese caso
     *    evaluateRecommendation devuelve ChatRecommendationOutcome.none(...) cuyo suggestedAction() es
     *    null; por eso suggestedAction != null nunca llega a ser true en ese punto.
     */

    @Test
    @DisplayName("Devuelve la respuesta del LLM sin acción sugerida cuando la recomendación es nula")
    void processMessageShouldReturnLlmReplyWithoutActionWhenRecommendationIsNull() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(), ChatReply.of("respuesta simple"));
        givenNullRecommendation();
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "hola");
        // --- assert ---
        thenReturnedLlmReplyWithoutActionAndSavedMemory(result, "respuesta simple");
    }

    @Test
    @DisplayName("No ofrece la pregunta de estilo cuando la preferencia no está pendiente de estilo")
    void processMessageShouldNotOfferStyleWhenPreferenceIsNotPending() {
        // --- arrange ---
        givenConversationPreference(ChatConversationPreference.builder()
                .id(5L)
                .userId(1L)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .build());
        givenOfferStyleWhenSafe(1L, "conv-1", "todo bien");
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("Todo bien por acá.", EmotionType.JOY, 3, false, null));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "todo bien");
        // --- assert ---
        thenStyleQuestionNotOfferedNorSaved(result);
    }

    @Test
    @DisplayName("No persiste el reto generado cuando no es recordable")
    void processMessageShouldNotPersistGeneratedChallengeWhenNotRememberable() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("respuesta", EmotionType.JOY, 3, false, null, null,
                        new ChatReply.GeneratedChallenge("", "descripcion")));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "hola");
        // --- assert ---
        thenChallengeNotPersisted(result);
    }

    @Test
    @DisplayName("Conserva el reto del LLM cuando el usuario pide un reto y la respuesta ya incluye uno")
    void processMessageShouldKeepLlmChallengeWhenUserRequestsChallengeAndReplyAlreadyHasOne() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(),
                new ChatReply("Aquí tenés tu reto.", EmotionType.MOTIVATION, 5, false, null, null,
                        new ChatReply.GeneratedChallenge("Reto del LLM", "Hacé esto")));
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "quiero un reto");
        // --- assert ---
        thenChallengeKeptFromLlm(result, "Reto del LLM", "Aquí tenés tu reto.");
    }

    @Test
    @DisplayName("No propaga errores de persistencia del historial y guarda la memoria del usuario igual")
    void processMessageShouldNotPropagatePersistenceErrorsAndStillSaveMemory() {
        // --- arrange ---
        givenDefaultSetup("", List.of(), "prompt", List.of(), ChatReply.of("respuesta ok"));
        givenChatMemoryPersistenceFails();
        // --- act ---
        ChatReplyResponse result = execute(1L, "conv-1", "hola");
        // --- assert ---
        thenReplyReturnedAndMemorySavedDespitePersistenceError(result, "respuesta ok");
    }

    // --- arrange ---

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

    private void givenPreferenceHandled(long userId, String conversationId, String message, ChatReply reply) {
        when(chatPreferenceHandlingService.handle(userId, conversationId, message))
                .thenReturn(ChatPreferenceHandlingResult.handled(reply));
    }

    private void givenRegisteredUser(long userId, String name) {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(AppUser.builder().id(userId).name(name).build()));
    }

    private void givenConversationPreference(ChatConversationPreference preference) {
        when(chatConversationPreferenceRepository.findByUserId(preference.getUserId()))
                .thenReturn(Optional.of(preference));
    }

    private void givenActiveRiskWord(String word, RiskSeverity severity) {
        RiskWord riskWord = RiskWord.builder().id(1L).word(word).severity(severity).active(true).build();
        givenDefaultSetup("", List.of(riskWord), "enriquecido", List.of(), ChatReply.of("ok"));
    }

    private void givenSetupWithRelevantMemory(long userId, String message) {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
        when(userVectorMemoryService.findRelevantUserMemories(userId, message))
                .thenReturn(List.of(rememberedMemory()));
        when(riskWordRepository.findAllActive()).thenReturn(List.of());
        when(promptBuilderService.buildEnrichedPrompt(any(), any(), eq(List.of(rememberedMemory())), any(), any(), any()))
                .thenReturn("prompt final");
        when(chatMemoryPort.getHistory(anyString(), anyLong())).thenReturn(List.of());
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("ok"));
    }

    private void givenEmotionalRecommendation(SuggestedChatAction action, EmotionalAnalysisResult analysis) {
        when(chatEmotionalRecommendationService.recommend(any(), any(), any(), any(), any(), any(), eq(false)))
                .thenReturn(new ChatRecommendationOutcome(analysis, action));
    }

    private void givenForcedActivityRecommendation(SuggestedChatAction action) {
        when(chatEmotionalRecommendationService.recommend(any(), any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(new ChatRecommendationOutcome(EmotionalAnalysisResult.neutral(), action));
    }

    private void givenOfferStyleWhenSafe(long userId, String conversationId, String message) {
        when(chatPreferenceHandlingService.handle(userId, conversationId, message))
                .thenReturn(ChatPreferenceHandlingResult.continueChatAndOfferStyle());
    }

    private void givenNullRecommendation() {
        when(chatEmotionalRecommendationService.recommend(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(null);
    }

    private void givenChatMemoryPersistenceFails() {
        doThrow(new RuntimeException("fallo al persistir"))
                .when(chatMemoryPort).addMessage(any(), any(), any());
    }

    private SuggestedChatAction diaryAction() {
        return new SuggestedChatAction(
                ActivityType.DIARY,
                2L,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                "/api/activities",
                30L);
    }

    private List<ConversationMessage> conversationHistory() {
        return List.of(ConversationMessage.of(MessageRole.USER, "anterior"));
    }

    private VectorMemory rememberedMemory() {
        return new VectorMemory("mem-1", 1L, null, null, "recuerdo", null, 0.9);
    }

    // --- act ---

    private ChatReplyResponse execute(long userId, String conversationId, String message) {
        return chatUseCase.execute(new ChatMessageRequest(userId, conversationId, message));
    }

    // --- assert ---

    private void thenContentIs(ChatReplyResponse result, String expected) {
        assertThat(result.content()).isEqualTo(expected);
    }

    private void thenPreferenceServiceWasCalled(long userId, String conversationId, String message) {
        verify(chatPreferenceHandlingService).handle(userId, conversationId, message);
    }

    private void thenLlmWasNotCalled() {
        verifyNoInteractions(llmChatPort);
    }

    private void thenReplyMatchesEmotion(ChatReplyResponse result, String content, EmotionType emotion, int intensity) {
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.detectedEmotion()).isEqualTo(emotion);
        assertThat(result.intensity()).isEqualTo(intensity);
    }

    private void thenPromptBuiltWithBasePrompt(String basePrompt) {
        verify(promptBuilderService).buildEnrichedPrompt(eq(basePrompt), any(), any(), any(), any(), any());
    }

    private void thenPersonalizationPassedToPromptBuilder(String registeredName, String preferredName, CommunicationStyle style) {
        ArgumentCaptor<ChatPersonalizationContext> personalizationCaptor =
                ArgumentCaptor.forClass(ChatPersonalizationContext.class);
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), any(), any(), personalizationCaptor.capture());
        assertThat(personalizationCaptor.getValue().registeredName()).isEqualTo(registeredName);
        assertThat(personalizationCaptor.getValue().preferredName()).isEqualTo(preferredName);
        assertThat(personalizationCaptor.getValue().communicationStyle()).isEqualTo(style);
    }

    private void thenPromptBuiltWithActiveRiskWord(String word, RiskSeverity severity) {
        verify(promptBuilderService).buildEnrichedPrompt(
                any(),
                argThat((List<RiskWord> words) -> words.size() == 1
                        && word.equals(words.get(0).getWord())
                        && words.get(0).getSeverity() == severity
                        && words.get(0).isActive()),
                any(), any(), any(), any());
    }

    private void thenUserMessageSavedWith(String conversationId, long userId, String content,
                                          EmotionType emotion, String matchedWord) {
        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq(conversationId), captor.capture(), eq(userId));
        ConversationMessage userMsg = captor.getAllValues().get(0);
        assertThat(userMsg.role()).isEqualTo(MessageRole.USER);
        assertThat(userMsg.content()).isEqualTo(content);
        assertThat(userMsg.detectedEmotion()).isEqualTo(emotion);
        assertThat(userMsg.riskDetected()).isTrue();
        assertThat(userMsg.matchedWord()).isEqualTo(matchedWord);
    }

    private void thenAssistantMessageSavedWith(String conversationId, long userId, String content) {
        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq(conversationId), captor.capture(), eq(userId));
        ConversationMessage assistantMsg = captor.getAllValues().get(1);
        assertThat(assistantMsg.role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(assistantMsg.content()).isEqualTo(content);
    }

    private void thenHistoryFetchedAndPassedToLlm(String conversationId, long userId, String message) {
        verify(chatMemoryPort).getHistory(conversationId, userId);
        verify(llmChatPort).chat(any(), eq(message), eq(conversationHistory()));
    }

    private void thenReusableMemoryServiceUsed(long userId, String message) {
        verify(userVectorMemoryService).findRelevantUserMemories(userId, message);
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().content()).isEqualTo(message);
        verify(llmChatPort).chat(eq("prompt final"), any(), any());
    }

    private void thenSuggestedActionAttached(ChatReplyResponse result) {
        assertThat(result.suggestedAction().type()).isEqualTo(ActivityType.DIARY);
        assertThat(result.suggestedAction().activityId()).isEqualTo(2L);
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.SADNESS);
        assertThat(result.intensity()).isEqualTo(9);
    }

    private void thenActivityRecommendationForced(ChatReplyResponse result) {
        assertThat(result.suggestedAction().type()).isEqualTo(ActivityType.DIARY);
        assertThat(result.suggestedAction().activityId()).isEqualTo(2L);
        verify(chatEmotionalRecommendationService).recommend(any(), any(), any(), any(), any(), any(), eq(true));
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), eq(diaryAction()), eq(ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST), any());
    }

    private void thenChallengeForced(ChatReplyResponse result) {
        assertThat(result.generatedChallenge()).isNotNull();
        assertThat(result.generatedChallenge().title()).isEqualTo("Reto de accion pequena");
        verify(promptBuilderService).buildEnrichedPrompt(
                any(), any(), any(), eq(null), eq(ChatUserIntent.CHALLENGE_REQUEST), any());
    }

    private void thenStyleQuestionAppendedAndMarkedAsked(ChatReplyResponse result) {
        assertThat(result.content())
                .contains("Todo bien por acá.")
                .contains("Cómo te gustaría que te hable");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(chatConversationPreferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    private void thenStyleQuestionNotOfferedNorSaved(ChatReplyResponse result) {
        assertThat(result.content()).doesNotContain("Cómo te gustaría");
        verify(chatConversationPreferenceRepository, never()).save(any());
    }

    private void thenChallengeNullified(ChatReplyResponse result) {
        assertThat(result.generatedChallenge()).isNull();
    }

    private void thenReturnedLlmReplyWithoutActionAndSavedMemory(ChatReplyResponse result, String content) {
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.suggestedAction()).isNull();
        verify(userVectorMemoryService).saveMemory(any());
    }

    private void thenChallengeNotPersisted(ChatReplyResponse result) {
        assertThat(result.generatedChallenge()).isNotNull();
        verify(userVectorMemoryService, times(1)).saveMemory(any());
    }

    private void thenChallengeKeptFromLlm(ChatReplyResponse result, String title, String content) {
        assertThat(result.generatedChallenge()).isNotNull();
        assertThat(result.generatedChallenge().title()).isEqualTo(title);
        assertThat(result.content()).isEqualTo(content);
    }

    private void thenReplyReturnedAndMemorySavedDespitePersistenceError(ChatReplyResponse result, String content) {
        assertThat(result.content()).isEqualTo(content);
        verify(userVectorMemoryService).saveMemory(any());
    }
}
