package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatOnboardingPlanTest {

    @Test
    @DisplayName("Pregunta el nombre cuando esa pregunta está habilitada")
    void createShouldAskPreferredNameWhenEnabled() {
        ChatOnboardingPlan plan = ChatOnboardingPlan.create(true, true, "Sergio");

        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
        assertThat(plan.greeting())
                .contains("Hola Sergio")
                .contains("Cómo te gustaría que te llame");
    }

    @Test
    @DisplayName("Pregunta el estilo cuando el nombre está deshabilitado")
    void createShouldAskCommunicationStyleWhenNameDisabled() {
        ChatOnboardingPlan plan = ChatOnboardingPlan.create(false, true, "Sergio");

        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE);
        assertThat(plan.greeting()).contains("Cómo te gustaría que te hable");
    }

    @Test
    @DisplayName("Completa el onboarding y saluda en general cuando ambas preguntas están deshabilitadas")
    void createShouldCompleteWhenBothQuestionsDisabled() {
        ChatOnboardingPlan plan = ChatOnboardingPlan.create(false, false, "Sergio");

        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.COMPLETED);
        assertThat(plan.greeting())
                .contains("En qué te puedo ayudar")
                .doesNotContain("Cómo te gustaría");
    }

    @Test
    @DisplayName("Trata los flags nulos como habilitados")
    void createShouldTreatNullFlagsAsEnabled() {
        ChatOnboardingPlan plan = ChatOnboardingPlan.create(null, null, "Sergio");

        assertThat(plan.initialStatus()).isEqualTo(ChatOnboardingStatus.ASKED_PREFERRED_NAME);
    }

    @Test
    @DisplayName("Usa un saludo sin nombre cuando el nombre registrado es nulo o vacío")
    void createShouldUseGenericGreetingWhenNameIsBlank() {
        ChatOnboardingPlan plan = ChatOnboardingPlan.create(true, true, "  ");

        assertThat(plan.greeting()).startsWith("Hola, soy Huly");
    }
}
