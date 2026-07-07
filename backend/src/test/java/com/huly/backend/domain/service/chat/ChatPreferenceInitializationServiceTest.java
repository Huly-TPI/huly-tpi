package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatPreferenceInitializationServiceTest {

    private static final Long USER_ID = 1L;
    private static final String CONVERSATION_ID = "conv-1";

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
        givenNoExistingPreference();
        givenRegisteredUser("Sergio");
        givenChatConfig(true, true);

        ChatOnboardingInitialization result = initialize();

        thenInitialized(result);
        thenGreetingContains(result, "Hola Sergio", "Cómo te gustaría que te llame");
        thenPreferenceSavedForUserWithStatus(USER_ID, ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        thenGreetingPersistedAsAssistantMessage(result);
    }

    @Test
    @DisplayName("No duplica el saludo cuando la preferencia ya existe")
    void initializeShouldNotDuplicateGreetingWhenPreferencesAlreadyExist() {
        givenExistingPreference();

        ChatOnboardingInitialization result = initialize();

        thenNotInitialized(result);
        thenNothingPersisted();
    }

    @Test
    @DisplayName("Pregunta el estilo directamente cuando la pregunta del nombre está deshabilitada")
    void initializeShouldAskStyleDirectlyWhenNameQuestionIsDisabled() {
        givenNoExistingPreference();
        givenRegisteredUser("Sergio");
        givenChatConfig(false, true);

        ChatOnboardingInitialization result = initialize();

        thenGreetingContains(result, "Hola Sergio", "Cómo te gustaría que te hable");
        thenPreferenceSavedWithStatus(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
    }

    @Test
    @DisplayName("Crea un saludo general cuando ambas preguntas están deshabilitadas")
    void initializeShouldCreateGeneralGreetingWhenBothQuestionsAreDisabled() {
        givenNoExistingPreference();
        givenRegisteredUser("Sergio");
        givenChatConfig(false, false);

        ChatOnboardingInitialization result = initialize();

        thenGreetingContains(result, "En qué te puedo ayudar");
        thenGreetingDoesNotContain(result, "Cómo te gustaría");
        thenPreferenceSavedWithStatus(ChatOnboardingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Falla cuando el usuario no existe")
    void initializeShouldThrowWhenUserDoesNotExist() {
        givenNoExistingPreference();
        givenMissingUser();

        thenInitializeThrowsUserNotFound();
    }

    @Test
    @DisplayName("Usa el plan por defecto cuando no hay configuración del chatbot")
    void initializeShouldUseDefaultPlanWhenNoChatConfigExists() {
        givenNoExistingPreference();
        givenRegisteredUser("Sergio");
        givenNoChatConfig();

        ChatOnboardingInitialization result = initialize();

        thenInitialized(result);
        thenGreetingContains(result, "Hola Sergio", "Cómo te gustaría que te llame");
        thenPreferenceSavedWithStatus(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
    }

    // --- arrange ---
    private void givenNoExistingPreference() {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenExistingPreference() {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingPreference()));
    }

    private void givenRegisteredUser(String name) {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(AppUser.builder().id(USER_ID).name(name).build()));
    }

    private void givenMissingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenChatConfig(boolean preferredNameEnabled, boolean communicationStyleEnabled) {
        when(chatConfigRepository.findFirst())
                .thenReturn(Optional.of(new ChatConfig(1L, true, "prompt", preferredNameEnabled, communicationStyleEnabled)));
    }

    private void givenNoChatConfig() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    private ChatConversationPreference existingPreference() {
        return ChatConversationPreference.builder()
                .id(10L)
                .userId(USER_ID)
                .onboardingStatus(ChatOnboardingStatus.ASKED_PREFERRED_NAME)
                .build();
    }

    // --- act ---
    private ChatOnboardingInitialization initialize() {
        return service.initialize(USER_ID, CONVERSATION_ID);
    }

    // --- assert ---
    private void thenInitialized(ChatOnboardingInitialization result) {
        assertThat(result.initialized()).isTrue();
    }

    private void thenNotInitialized(ChatOnboardingInitialization result) {
        assertThat(result.initialized()).isFalse();
        assertThat(result.assistantMessage()).isNull();
    }

    private void thenGreetingContains(ChatOnboardingInitialization result, String... fragments) {
        assertThat(result.assistantMessage()).contains(fragments);
    }

    private void thenGreetingDoesNotContain(ChatOnboardingInitialization result, String fragment) {
        assertThat(result.assistantMessage()).doesNotContain(fragment);
    }

    private void thenPreferenceSavedWithStatus(ChatOnboardingStatus status) {
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingStatus()).isEqualTo(status);
    }

    private void thenPreferenceSavedForUserWithStatus(Long userId, ChatOnboardingStatus status) {
        ArgumentCaptor<ChatConversationPreference> captor =
                ArgumentCaptor.forClass(ChatConversationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getOnboardingStatus()).isEqualTo(status);
    }

    private void thenGreetingPersistedAsAssistantMessage(ChatOnboardingInitialization result) {
        ArgumentCaptor<ConversationMessage> captor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(chatMemoryPort).addMessage(eq(CONVERSATION_ID), captor.capture(), eq(USER_ID));
        assertThat(captor.getValue().role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(captor.getValue().content()).isEqualTo(result.assistantMessage());
    }

    private void thenNothingPersisted() {
        verify(preferenceRepository, never()).save(any());
        verify(chatMemoryPort, never()).addMessage(any(), any(), any());
    }

    private void thenInitializeThrowsUserNotFound() {
        assertThatThrownBy(() -> service.initialize(USER_ID, CONVERSATION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
