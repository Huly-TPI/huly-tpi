package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceDetectionService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleChatPreferencesUseCaseTest {

    @Mock
    private ChatConversationPreferenceRepository preferenceRepository;
    @Mock
    private InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    @Mock
    private ChatMemoryPort chatMemoryPort;
    @Mock
    private ChatQuotaService chatQuotaService;

    private HandleChatPreferencesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HandleChatPreferencesUseCase(
                preferenceRepository,
                new ChatPreferenceDetectionService(),
                initializeChatPreferencesUseCase,
                chatMemoryPort,
                chatQuotaService);
    }

    @Test
    void execute_shouldSavePreferredNameAndAskForCommunicationStyle() {
        when(preferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(preference(ChatOnboardingStatus.ASKED_PREFERRED_NAME)));

        Optional<ChatReply> result = useCase.execute(1L, "conv-1", "Sergito");

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("Perfecto, Sergito").contains("Cómo te gustaría");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Sergito");
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
        verify(chatMemoryPort, org.mockito.Mockito.times(2))
                .addMessage(org.mockito.ArgumentMatchers.eq("conv-1"), any(), org.mockito.ArgumentMatchers.eq(1L));
    }

    @Test
    void execute_shouldSaveCommunicationStyleAndCompleteOnboarding() {
        ChatConversationPreference preference = preference(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
        preference = preference.withPreferredName("Sergito", Instant.now());
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));

        Optional<ChatReply> result = useCase.execute(1L, "conv-1", "informal");

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("estilo informal");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getCommunicationStyle()).isEqualTo(CommunicationStyle.INFORMAL);
        assertThat(captor.getValue().getOnboardingStatus()).isEqualTo(ChatOnboardingStatus.COMPLETED);
    }

    @Test
    void execute_shouldUpdatePreferredNameAfterOnboarding() {
        when(preferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(completedPreference()));

        Optional<ChatReply> result =
                useCase.execute(1L, "conv-1", "Desde ahora llamame Checho");

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("Checho");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Checho");
        assertThat(captor.getValue().getCommunicationStyle()).isEqualTo(CommunicationStyle.FRIENDLY);
    }

    @Test
    void execute_shouldUpdateCommunicationStyleAfterOnboarding() {
        when(preferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(completedPreference()));

        Optional<ChatReply> result = useCase.execute(1L, "conv-1", "Hablame más directo");

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("estilo directo");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getCommunicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
    }

    @Test
    void execute_shouldLeaveNormalConversationMessageForChatService() {
        when(preferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(completedPreference()));

        Optional<ChatReply> result = useCase.execute(
                1L,
                "conv-1",
                "Mi amigo me habló muy directo y me hizo sentir mal");

        assertThat(result).isEmpty();
        verify(preferenceRepository, never()).save(any());
        verify(chatMemoryPort, never()).addMessage(any(), any(), any());
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

    private ChatConversationPreference completedPreference() {
        return ChatConversationPreference.builder()
                .id(10L)
                .userId(1L)
                .preferredName("Sergio")
                .communicationStyle(CommunicationStyle.FRIENDLY)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }
}
