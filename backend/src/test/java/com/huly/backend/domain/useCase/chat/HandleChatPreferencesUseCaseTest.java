package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceResolutionService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleChatPreferencesUseCaseTest {

    @Mock private ChatConversationPreferenceRepository preferenceRepository;
    @Mock private ChatPreferenceResolutionService resolutionService;
    @Mock private InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    @Mock private ChatMemoryPort chatMemoryPort;
    @Mock private ChatQuotaService chatQuotaService;
    @Mock private ChatConfigRepository chatConfigRepository;

    private HandleChatPreferencesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HandleChatPreferencesUseCase(
                preferenceRepository,
                resolutionService,
                initializeChatPreferencesUseCase,
                chatMemoryPort,
                chatQuotaService,
                chatConfigRepository);
        lenient().when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", true, true)));
    }

    @Test
    void execute_shouldSavePreferredNameAndAskForCommunicationStyle() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve("Sergito", ChatPreferenceExpectedField.PREFERRED_NAME))
                .thenReturn(result("Sergito", null, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = useCase.execute(1L, "conv-1", "Sergito");

        assertThat(result.continueConversation()).isFalse();
        assertThat(result.directReply().content())
                .contains("Perfecto, Sergito")
                .contains("Cómo te gustaría");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Sergito");
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    void execute_shouldSaveNameAndContinueForMixedMessage() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve(
                "Llamame crack y estoy triste",
                ChatPreferenceExpectedField.PREFERRED_NAME))
                .thenReturn(result("Crack", null, ChatPreferenceMessageType.MIXED));

        ChatPreferenceHandlingResult result =
                useCase.execute(1L, "conv-1", "Llamame crack y estoy triste");

        assertThat(result.continueConversation()).isTrue();
        assertThat(result.offerCommunicationStyleWhenSafe()).isTrue();
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Crack");
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE);
        verify(chatMemoryPort, never()).addMessage(any(), any(), any());
        verify(chatQuotaService, never()).assertWithinLimit(any());
    }

    @Test
    void execute_shouldSkipOnboardingWhenMessageDoesNotAnswerNameQuestion() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve(
                "Necesito hablar de algo",
                ChatPreferenceExpectedField.PREFERRED_NAME))
                .thenReturn(ChatPreferenceDetectionResult.unrelated());

        ChatPreferenceHandlingResult result =
                useCase.execute(1L, "conv-1", "Necesito hablar de algo");

        assertThat(result.continueConversation()).isTrue();
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.COMPLETED);
    }

    @Test
    void execute_shouldSaveCommunicationStyleAndCompleteOnboarding() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE)
                .withPreferredName("Sergito", Instant.now());
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve("informal", ChatPreferenceExpectedField.COMMUNICATION_STYLE))
                .thenReturn(result(null, CommunicationStyle.INFORMAL, ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result = useCase.execute(1L, "conv-1", "informal");

        assertThat(result.continueConversation()).isFalse();
        assertThat(result.directReply().content()).contains("estilo informal");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getCommunicationStyle()).isEqualTo(CommunicationStyle.INFORMAL);
        assertThat(captor.getValue().getOnboardingStatus()).isEqualTo(ChatOnboardingStatus.COMPLETED);
    }

    @Test
    void execute_shouldKeepPendingStyleUntilAResponseCanOfferIt() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .updatePreferredName("Crack", Instant.now());
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve("Estoy muy mal", ChatPreferenceExpectedField.ANY))
                .thenReturn(ChatPreferenceDetectionResult.unrelated());

        ChatPreferenceHandlingResult result = useCase.execute(1L, "conv-1", "Estoy muy mal");

        assertThat(result.continueConversation()).isTrue();
        assertThat(result.offerCommunicationStyleWhenSafe()).isTrue();
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    void execute_shouldApplyBothPreferencesFromOneMessage() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(resolutionService.resolve(
                "Decime Crack y hablame directo",
                ChatPreferenceExpectedField.PREFERRED_NAME))
                .thenReturn(result(
                        "Crack",
                        CommunicationStyle.DIRECT,
                        ChatPreferenceMessageType.PREFERENCE_ONLY));

        ChatPreferenceHandlingResult result =
                useCase.execute(1L, "conv-1", "Decime Crack y hablame directo");

        assertThat(result.directReply().content())
                .contains("Crack")
                .contains("estilo directo");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Crack");
        assertThat(captor.getValue().getCommunicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
        assertThat(captor.getValue().getOnboardingStatus()).isEqualTo(ChatOnboardingStatus.COMPLETED);
    }

    private ChatPreferenceDetectionResult result(
            String name,
            CommunicationStyle style,
            ChatPreferenceMessageType type) {
        return new ChatPreferenceDetectionResult(name, style, type, 1.0);
    }

    private ChatConversationPreference preference(ChatOnboardingStatus status) {
        return ChatConversationPreference.builder()
                .id(10L)
                .userId(1L)
                .onboardingStatus(status)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }
}
