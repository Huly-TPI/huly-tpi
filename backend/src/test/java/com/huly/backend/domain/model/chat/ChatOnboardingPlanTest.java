package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatOnboardingPlanTest {

    @Test
    @DisplayName("Pregunta el nombre cuando esa pregunta está habilitada")
    void createShouldAskPreferredNameWhenEnabled() {
        ChatOnboardingPlan plan = createPlan(true, true, "Sergio");

        thenAsksPreferredName(plan);
        thenGreetingAsksName(plan);
    }

    @Test
    @DisplayName("Pregunta el estilo cuando el nombre está deshabilitado")
    void createShouldAskCommunicationStyleWhenNameDisabled() {
        ChatOnboardingPlan plan = createPlan(false, true, "Sergio");

        thenAsksCommunicationStyle(plan);
    }

    @Test
    @DisplayName("Completa el onboarding y saluda en general cuando ambas preguntas están deshabilitadas")
    void createShouldCompleteWhenBothQuestionsDisabled() {
        ChatOnboardingPlan plan = createPlan(false, false, "Sergio");

        thenCompletesWithGeneralGreeting(plan);
    }

    @Test
    @DisplayName("Trata los flags nulos como habilitados")
    void createShouldTreatNullFlagsAsEnabled() {
        ChatOnboardingPlan plan = createPlan(null, null, "Sergio");

        thenAsksPreferredName(plan);
    }

    @Test
    @DisplayName("Usa un saludo sin nombre cuando el nombre registrado está en blanco")
    void createShouldUseGenericGreetingWhenNameIsBlank() {
        ChatOnboardingPlan plan = createPlan(true, true, "  ");

        thenGreetingHasNoName(plan);
    }

    @Test
    @DisplayName("Usa un saludo sin nombre cuando el nombre registrado es nulo")
    void createShouldUseGenericGreetingWhenNameIsNull() {
        ChatOnboardingPlan plan = createPlan(true, true, null);

        thenGreetingHasNoName(plan);
    }

    // --- act ---

    private ChatOnboardingPlan createPlan(
            Boolean preferredNameQuestionEnabled,
            Boolean communicationStyleQuestionEnabled,
            String registeredName) {
        return ChatOnboardingPlan.create(
                preferredNameQuestionEnabled, communicationStyleQuestionEnabled, registeredName);
    }

    // --- assert ---

    private void thenAsksPreferredName(ChatOnboardingPlan plan) {
        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
    }

    private void thenGreetingAsksName(ChatOnboardingPlan plan) {
        assertThat(plan.greeting())
                .contains("Hola Sergio")
                .contains("Cómo te gustaría que te llame");
    }

    private void thenAsksCommunicationStyle(ChatOnboardingPlan plan) {
        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
        assertThat(plan.greeting()).contains("Cómo te gustaría que te hable");
    }

    private void thenCompletesWithGeneralGreeting(ChatOnboardingPlan plan) {
        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.COMPLETED);
        assertThat(plan.greeting())
                .contains("En qué te puedo ayudar")
                .doesNotContain("Cómo te gustaría");
    }

    private void thenGreetingHasNoName(ChatOnboardingPlan plan) {
        assertThat(plan.greeting()).startsWith("Hola, soy Huly");
    }
}
