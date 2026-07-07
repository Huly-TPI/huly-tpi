package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.port.ChatPreferenceExtractionPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatPreferenceHandlingServiceTest {

    private static final Long USER_ID = 1L;
    private static final String CONVERSATION_ID = "conv-1";

    @Mock private ChatConversationPreferenceRepository preferenceRepository;
    @Mock private ChatPreferenceExtractionPort extractionPort;
    @Mock private ChatPreferenceInitializationService chatPreferenceInitializationService;
    @Mock private ChatMemoryPort chatMemoryPort;
    @Mock private ChatQuotaService chatQuotaService;
    @Mock private ChatConfigRepository chatConfigRepository;

    private ChatPreferenceHandlingService service;

    @BeforeEach
    void setUp() {
        service = new ChatPreferenceHandlingService(
                preferenceRepository,
                extractionPort,
                chatPreferenceInitializationService,
                chatMemoryPort,
                chatQuotaService,
                chatConfigRepository);
        lenient().when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", true, true)));
    }

    // ===== handle: sin preferencia previa (onboarding) =====

    @Test
    @DisplayName("Devuelve la respuesta generada cuando se inicializa el onboarding")
    void handleShouldReturnHandledWhenOnboardingInitialized() {
        givenNoStoredPreference();
        givenOnboardingInitialization(new ChatOnboardingInitialization(true, "Hola, ¿cómo te gustaría que te llame?"));

        ChatPreferenceHandlingResult result = handle("Hola");

        thenHandled(result);
        thenReplyIs(result, "Hola, ¿cómo te gustaría que te llame?");
        thenQuotaChecked();
        thenOnboardingInitialized();
    }

    @Test
    @DisplayName("Continúa la conversación cuando el onboarding no se inicializa")
    void handleShouldContinueChatWhenOnboardingNotInitialized() {
        givenNoStoredPreference();
        givenOnboardingInitialization(ChatOnboardingInitialization.existing());

        ChatPreferenceHandlingResult result = handle("Hola");

        thenContinuesChat(result);
        thenQuotaChecked();
    }

    // ===== handle: estado ASKED_PREFERRED_NAME =====

    @Test
    @DisplayName("Guarda el nombre preferido y pregunta por el estilo de comunicación")
    void handleShouldSavePreferredNameAndAskForCommunicationStyle() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Sergito", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Sergito", null, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Sergito");

        thenHandled(result);
        thenReplyContains(result, "Perfecto, Sergito", "Cómo te gustaría");
        thenSavedPreference("Sergito", null, ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    @DisplayName("Guarda el nombre y continúa la conversación en un mensaje mixto")
    void handleShouldSaveNameAndContinueForMixedMessage() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Llamame crack y estoy triste", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Crack", null, ChatPreferenceMessageType.MIXED));

        ChatPreferenceHandlingResult result = handle("Llamame crack y estoy triste");

        thenContinuesChatOfferingStyle(result);
        thenSavedPreference("Crack", null, ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE);
        thenNoExchangeSaved();
        thenQuotaNotChecked();
    }

    @Test
    @DisplayName("Salta el onboarding cuando el mensaje no responde a la pregunta del nombre")
    void handleShouldSkipOnboardingWhenMessageDoesNotAnswerNameQuestion() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtractionFails("Necesito hablar de algo", ChatPreferenceExpectedField.PREFERRED_NAME);

        ChatPreferenceHandlingResult result = handle("Necesito hablar de algo");

        thenContinuesChat(result);
        thenSavedStatus(ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Aplica ambas preferencias a partir de un solo mensaje")
    void handleShouldApplyBothPreferencesFromOneMessage() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Decime Crack y hablame directo", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Crack", CommunicationStyle.DIRECT, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Decime Crack y hablame directo");

        thenReplyContains(result, "Crack", "estilo directo");
        thenSavedPreference("Crack", CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Completa el onboarding con solo el nombre cuando la pregunta de estilo está deshabilitada")
    void handleShouldCompleteWhenStyleQuestionDisabledAndNameOnly() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Decime Crack", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Crack", null, ChatPreferenceMessageType.PREFERENCE_ONLY));
        givenCommunicationStyleQuestionDisabled();

        ChatPreferenceHandlingResult result = handle("Decime Crack");

        thenHandled(result);
        thenReplyIs(result, "Perfecto, Crack.");
        thenSavedPreference("Crack", null, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Pregunta por el estilo cuando el flag de configuración es nulo")
    void handleShouldAskStyleWhenConfigFlagIsNullAndNameOnly() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Decime Nico", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Nico", null, ChatPreferenceMessageType.PREFERENCE_ONLY));
        givenCommunicationStyleQuestionFlagNull();

        ChatPreferenceHandlingResult result = handle("Decime Nico");

        thenReplyContains(result, "Perfecto, Nico", "Cómo te gustaría");
        thenSavedPreference("Nico", null, ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    @DisplayName("Pregunta por el estilo cuando no existe configuración de chat")
    void handleShouldAskStyleWhenNoChatConfigExists() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Decime Nico", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Nico", null, ChatPreferenceMessageType.PREFERENCE_ONLY));
        givenChatConfigMissing();

        ChatPreferenceHandlingResult result = handle("Decime Nico");

        thenReplyContains(result, "Perfecto, Nico", "Cómo te gustaría");
        thenSavedPreference("Nico", null, ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    @DisplayName("Guarda solo el estilo cuando el mensaje de nombre solo trae estilo")
    void handleShouldSaveStyleFromNameQuestionWhenOnlyStyleDetected() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Hablame informal", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection(null, CommunicationStyle.INFORMAL, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Hablame informal");

        thenReplyIs(result, "Entendido. Desde ahora voy a hablarte con un estilo informal.");
        thenSavedPreference(null, CommunicationStyle.INFORMAL, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Continúa la conversación en un mensaje mixto que ya trae el estilo")
    void handleShouldContinueChatWhenMixedMessageAlreadyHasStyle() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME));
        givenExtraction("Decime Crack pero estoy triste", ChatPreferenceExpectedField.PREFERRED_NAME,
                detection("Crack", CommunicationStyle.DIRECT, ChatPreferenceMessageType.MIXED));

        ChatPreferenceHandlingResult result = handle("Decime Crack pero estoy triste");

        thenContinuesChat(result);
        thenSavedPreference("Crack", CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
        thenNoExchangeSaved();
    }

    // ===== handle: estado PENDING_COMMUNICATION_STYLE =====

    @Test
    @DisplayName("Mantiene el estilo pendiente hasta que una respuesta pueda ofrecerlo")
    void handleShouldKeepPendingStyleUntilAResponseCanOfferIt() {
        givenStoredPreference(preferenceWithName(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE, "Crack"));

        ChatPreferenceHandlingResult result = handle("Estoy muy mal");

        thenContinuesChatOfferingStyle(result);
        thenNoPreferenceSaved();
    }

    @Test
    @DisplayName("Aplica el estilo pendiente cuando el mensaje trae una señal de cambio")
    void handleShouldApplyStyleWhenPendingAndChangeSignalDetected() {
        givenStoredPreference(preference(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE));
        givenExtraction("Hablame directo", ChatPreferenceExpectedField.ANY,
                detection(null, CommunicationStyle.DIRECT, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Hablame directo");

        thenHandled(result);
        thenReplyIs(result, "Entendido. Desde ahora voy a hablarte con un estilo directo.");
        thenSavedPreference(null, CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
        thenExchangeSaved(result, "Hablame directo");
        thenQuotaChecked();
    }

    // ===== handle: estado ASKED_COMMUNICATION_STYLE =====

    @Test
    @DisplayName("Guarda el estilo de comunicación y completa el onboarding")
    void handleShouldSaveCommunicationStyleAndCompleteOnboarding() {
        givenStoredPreference(preferenceWithName(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE, "Sergito"));
        givenExtraction("informal", ChatPreferenceExpectedField.COMMUNICATION_STYLE,
                detection(null, CommunicationStyle.INFORMAL, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("informal");

        thenHandled(result);
        thenReplyIs(result, "Entendido, Sergito. Desde ahora voy a hablarte con un estilo informal.");
        thenSavedPreference("Sergito", CommunicationStyle.INFORMAL, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Completa el onboarding cuando la extracción del estilo devuelve nulo")
    void handleShouldCompleteWhenAskedStyleButExtractionReturnsNull() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE));
        givenExtractionReturnsNull("no se", ChatPreferenceExpectedField.COMMUNICATION_STYLE);

        ChatPreferenceHandlingResult result = handle("no se");

        thenContinuesChat(result);
        thenSavedStatus(ChatOnboardingStatus.COMPLETED);
        thenNoExchangeSaved();
    }

    @Test
    @DisplayName("Continúa la conversación cuando el estilo llega en un mensaje mixto")
    void handleShouldContinueChatWhenAskedStyleAndMixedMessage() {
        givenStoredPreference(preference(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE));
        givenExtraction("directo, ayudame con esto", ChatPreferenceExpectedField.COMMUNICATION_STYLE,
                detection(null, CommunicationStyle.DIRECT, ChatPreferenceMessageType.MIXED));

        ChatPreferenceHandlingResult result = handle("directo, ayudame con esto");

        thenContinuesChat(result);
        thenSavedPreference(null, CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
        thenNoExchangeSaved();
    }

    @Test
    @DisplayName("Omite el nombre en la respuesta cuando el nombre preferido está en blanco")
    void handleShouldOmitBlankPreferredNameSuffixWhenApplyingStyle() {
        givenStoredPreference(preferenceWithName(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE, "   "));
        givenExtraction("formal", ChatPreferenceExpectedField.COMMUNICATION_STYLE,
                detection(null, CommunicationStyle.FORMAL, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("formal");

        thenReplyIs(result, "Entendido. Desde ahora voy a hablarte con un estilo formal.");
        thenSavedPreference("   ", CommunicationStyle.FORMAL, ChatOnboardingStatus.COMPLETED);
    }

    // ===== handle: estado COMPLETED (cambio de preferencias) =====

    @Test
    @DisplayName("Continúa la conversación cuando ya está completo y no hay señal de cambio")
    void handleShouldContinueChatWhenCompletedAndNoChangeSignal() {
        givenStoredPreference(preference(ChatOnboardingStatus.COMPLETED));

        ChatPreferenceHandlingResult result = handle("Gracias por todo");

        thenContinuesChat(result);
        thenNoPreferenceSaved();
    }

    @Test
    @DisplayName("Actualiza nombre y estilo cuando ya está completo y se detectan ambos")
    void handleShouldUpdateBothPreferencesWhenCompletedAndBothDetected() {
        givenStoredPreference(preference(ChatOnboardingStatus.COMPLETED));
        givenExtraction("Decime Crack y hablame directo", ChatPreferenceExpectedField.ANY,
                detection("Crack", CommunicationStyle.DIRECT, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Decime Crack y hablame directo");

        thenHandled(result);
        thenReplyIs(result,
                "Listo, de ahora en adelante te voy a decir Crack y voy a hablarte con un estilo directo.");
        thenSavedPreference("Crack", CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
        thenExchangeSaved(result, "Decime Crack y hablame directo");
        thenQuotaChecked();
    }

    @Test
    @DisplayName("Actualiza solo el nombre cuando ya está completo y solo se detecta el nombre")
    void handleShouldUpdateOnlyNameWhenCompletedAndNameDetected() {
        givenStoredPreference(preference(ChatOnboardingStatus.COMPLETED));
        givenExtraction("Decime Crack", ChatPreferenceExpectedField.ANY,
                detection("Crack", null, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Decime Crack");

        thenReplyIs(result, "Listo, de ahora en adelante te voy a decir Crack.");
        thenSavedPreference("Crack", null, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Actualiza solo el estilo cuando ya está completo y solo se detecta el estilo")
    void handleShouldUpdateOnlyStyleWhenCompletedAndStyleDetected() {
        givenStoredPreference(preference(ChatOnboardingStatus.COMPLETED));
        givenExtraction("Hablame directo", ChatPreferenceExpectedField.ANY,
                detection(null, CommunicationStyle.DIRECT, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = handle("Hablame directo");

        thenReplyIs(result, "Entendido. Desde ahora voy a hablarte con un estilo directo.");
        thenSavedPreference(null, CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Continúa la conversación cuando el cambio de preferencia llega en un mensaje mixto")
    void handleShouldContinueChatWhenCompletedAndMixedMessage() {
        givenStoredPreference(preference(ChatOnboardingStatus.COMPLETED));
        givenExtraction("Decime Crack, estoy triste", ChatPreferenceExpectedField.ANY,
                detection("Crack", CommunicationStyle.DIRECT, ChatPreferenceMessageType.MIXED));

        ChatPreferenceHandlingResult result = handle("Decime Crack, estoy triste");

        thenContinuesChat(result);
        thenSavedPreference("Crack", CommunicationStyle.DIRECT, ChatOnboardingStatus.COMPLETED);
        thenNoExchangeSaved();
    }

    // --- arrange ---
    private void givenNoStoredPreference() {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenStoredPreference(ChatConversationPreference preference) {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preference));
    }

    private void givenOnboardingInitialization(ChatOnboardingInitialization initialization) {
        when(chatPreferenceInitializationService.initialize(USER_ID, CONVERSATION_ID))
                .thenReturn(initialization);
    }

    private void givenExtraction(
            String message,
            ChatPreferenceExpectedField field,
            ChatPreferenceDetectionResult detection) {
        when(extractionPort.extract(message, field)).thenReturn(detection);
    }

    private void givenExtractionReturnsNull(String message, ChatPreferenceExpectedField field) {
        when(extractionPort.extract(message, field)).thenReturn(null);
    }

    private void givenExtractionFails(String message, ChatPreferenceExpectedField field) {
        when(extractionPort.extract(message, field)).thenThrow(new RuntimeException("API Error"));
    }

    private void givenCommunicationStyleQuestionDisabled() {
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", true, false)));
    }

    private void givenCommunicationStyleQuestionFlagNull() {
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", true, null)));
    }

    private void givenChatConfigMissing() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    private ChatConversationPreference preference(ChatOnboardingStatus status) {
        return preferenceBuilder(status).build();
    }

    private ChatConversationPreference preferenceWithName(ChatOnboardingStatus status, String name) {
        return preferenceBuilder(status).preferredName(name).build();
    }

    private ChatConversationPreference.ChatConversationPreferenceBuilder preferenceBuilder(
            ChatOnboardingStatus status) {
        return ChatConversationPreference.builder()
                .id(10L)
                .userId(USER_ID)
                .onboardingStatus(status)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    }

    private ChatPreferenceDetectionResult detection(
            String name,
            CommunicationStyle style,
            ChatPreferenceMessageType type) {
        return new ChatPreferenceDetectionResult(name, style, type, 1.0);
    }

    // --- act ---
    private ChatPreferenceHandlingResult handle(String message) {
        return service.handle(USER_ID, CONVERSATION_ID, message);
    }

    // --- assert ---
    private void thenHandled(ChatPreferenceHandlingResult result) {
        assertThat(result.continueConversation()).isFalse();
        assertThat(result.offerCommunicationStyleWhenSafe()).isFalse();
        assertThat(result.directReply()).isNotNull();
    }

    private void thenReplyIs(ChatPreferenceHandlingResult result, String expected) {
        assertThat(result.directReply().content()).isEqualTo(expected);
    }

    private void thenReplyContains(ChatPreferenceHandlingResult result, String... fragments) {
        assertThat(result.directReply().content()).contains(fragments);
    }

    private void thenContinuesChat(ChatPreferenceHandlingResult result) {
        assertThat(result.continueConversation()).isTrue();
        assertThat(result.offerCommunicationStyleWhenSafe()).isFalse();
        assertThat(result.directReply()).isNull();
    }

    private void thenContinuesChatOfferingStyle(ChatPreferenceHandlingResult result) {
        assertThat(result.continueConversation()).isTrue();
        assertThat(result.offerCommunicationStyleWhenSafe()).isTrue();
        assertThat(result.directReply()).isNull();
    }

    private void thenSavedPreference(
            String name,
            CommunicationStyle style,
            ChatOnboardingStatus status) {
        ChatConversationPreference saved = capturedSavedPreference();
        assertThat(saved.getPreferredName()).isEqualTo(name);
        assertThat(saved.getCommunicationStyle()).isEqualTo(style);
        assertThat(saved.getOnboardingStatus()).isEqualTo(status);
    }

    private void thenSavedStatus(ChatOnboardingStatus status) {
        assertThat(capturedSavedPreference().getOnboardingStatus()).isEqualTo(status);
    }

    private ChatConversationPreference capturedSavedPreference() {
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        return captor.getValue();
    }

    private void thenNoPreferenceSaved() {
        verify(preferenceRepository, never()).save(any());
    }

    private void thenExchangeSaved(ChatPreferenceHandlingResult result, String userMessage) {
        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort, times(2)).addMessage(eq(CONVERSATION_ID), captor.capture(), eq(USER_ID));
        List<ConversationMessage> messages = captor.getAllValues();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo(MessageRole.USER);
        assertThat(messages.get(0).content()).isEqualTo(userMessage);
        assertThat(messages.get(1).role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(messages.get(1).content()).isEqualTo(result.directReply().content());
    }

    private void thenNoExchangeSaved() {
        verify(chatMemoryPort, never()).addMessage(any(), any(), any());
    }

    private void thenQuotaChecked() {
        verify(chatQuotaService).assertWithinLimit(USER_ID);
    }

    private void thenQuotaNotChecked() {
        verify(chatQuotaService, never()).assertWithinLimit(any());
    }

    private void thenOnboardingInitialized() {
        verify(chatPreferenceInitializationService).initialize(USER_ID, CONVERSATION_ID);
    }
}
