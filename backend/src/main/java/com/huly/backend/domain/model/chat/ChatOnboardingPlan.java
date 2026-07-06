package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;

/**
 * Plan de onboarding derivado de la configuración del chatbot: el estado inicial de la
 * preferencia y el saludo con el que arranca la conversación. Regla de dominio pura (sin I/O):
 * interpreta los flags de configuración (nulo = habilitado) y arma el mensaje de bienvenida.
 */
public record ChatOnboardingPlan(ChatOnboardingStatus initialStatus, String greeting) {

    public static ChatOnboardingPlan create(
            Boolean preferredNameQuestionEnabled,
            Boolean communicationStyleQuestionEnabled,
            String registeredName) {
        boolean askPreferredName = preferredNameQuestionEnabled == null || preferredNameQuestionEnabled;
        boolean askCommunicationStyle = communicationStyleQuestionEnabled == null || communicationStyleQuestionEnabled;
        return new ChatOnboardingPlan(
                resolveInitialStatus(askPreferredName, askCommunicationStyle),
                buildGreeting(registeredName, askPreferredName, askCommunicationStyle));
    }

    private static ChatOnboardingStatus resolveInitialStatus(
            boolean askPreferredName,
            boolean askCommunicationStyle) {
        if (askPreferredName) {
            return ChatOnboardingStatus.ASKED_PREFERRED_NAME;
        }
        if (askCommunicationStyle) {
            return ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE;
        }
        return ChatOnboardingStatus.COMPLETED;
    }

    private static String buildGreeting(
            String registeredName,
            boolean askPreferredName,
            boolean askCommunicationStyle) {
        String prefix = registeredName == null || registeredName.isBlank()
                ? "Hola, soy Huly, tu asistente en este recorrido."
                : "Hola " + registeredName.trim() + ", soy Huly, tu asistente en este recorrido.";
        if (askPreferredName) {
            return prefix + " ¿Cómo te gustaría que te llame de ahora en adelante?";
        }
        if (askCommunicationStyle) {
            return prefix + " " + CommunicationStyle.QUESTION_TEXT;
        }
        return prefix + " ¿En qué te puedo ayudar hoy?";
    }
}
