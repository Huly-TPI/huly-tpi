package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatPreferenceInitializationServiceTest {

    @Mock
    private ChatConversationPreferenceRepository preferenceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatMemoryPort chatMemoryPort;
    @Mock
    private ChatConfigRepository chatConfigRepository;
    @InjectMocks
    private ChatPreferenceInitializationService service;

    @Test
    @DisplayName("Crea el saludo con el nombre registrado y lo persiste como mensaje del asistente")
    void initializeShouldCreateGreetingWithRegisteredNameAndPersistItAsAssistantMessage() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(AppUser.builder().id(1L).name("Sergio").build()));
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", true, true)));

        ChatOnboardingInitialization result = service.initialize(1L, "conv-1");

        assertThat(result.initialized()).isTrue();
        assertThat(result.assistantMessage())
                .contains("Hola Sergio")
                .contains("Cómo te gustaría que te llame");

        ArgumentCaptor<ChatConversationPreference> preferenceCaptor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(preferenceCaptor.capture());
        assertThat(preferenceCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(preferenceCaptor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_PREFERRED_NAME);

        ArgumentCaptor<ConversationMessage> messageCaptor =
                ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort).addMessage(eq("conv-1"), messageCaptor.capture(), eq(1L));
        assertThat(messageCaptor.getValue().role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(messageCaptor.getValue().content()).isEqualTo(result.assistantMessage());
    }

    @Test
    @DisplayName("No duplica el saludo cuando la preferencia ya existe")
    void initializeShouldNotDuplicateGreetingWhenPreferencesAlreadyExist() {
        ChatConversationPreference existing = ChatConversationPreference.builder()
                .id(10L)
                .userId(1L)
                .onboardingStatus(ChatOnboardingStatus.ASKED_PREFERRED_NAME)
                .build();
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        ChatOnboardingInitialization result = service.initialize(1L, "conv-1");

        assertThat(result.initialized()).isFalse();
        assertThat(result.assistantMessage()).isNull();
        verify(preferenceRepository, never()).save(any());
        verify(chatMemoryPort, never()).addMessage(any(), any(), any());
    }

    @Test
    @DisplayName("Pregunta el estilo directamente cuando la pregunta del nombre está deshabilitada")
    void initializeShouldAskStyleDirectlyWhenNameQuestionIsDisabled() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(AppUser.builder().id(1L).name("Sergio").build()));
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", false, true)));

        ChatOnboardingInitialization result = service.initialize(1L, "conv-1");

        assertThat(result.assistantMessage())
                .contains("Hola Sergio")
                .contains("Cómo te gustaría que te hable");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    @DisplayName("Crea un saludo general cuando ambas preguntas están deshabilitadas")
    void initializeShouldCreateGeneralGreetingWhenBothQuestionsAreDisabled() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(AppUser.builder().id(1L).name("Sergio").build()));
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", false, false)));

        ChatOnboardingInitialization result = service.initialize(1L, "conv-1");

        assertThat(result.assistantMessage())
                .contains("En qué te puedo ayudar")
                .doesNotContain("Cómo te gustaría");
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus())
                .isEqualTo(ChatOnboardingStatus.COMPLETED);
    }
}
