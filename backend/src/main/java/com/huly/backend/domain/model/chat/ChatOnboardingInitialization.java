package com.huly.backend.domain.model.chat;

/**
 * Describes whether conversational onboarding was initialized.
 *
 * @param initialized whether a new preference state and greeting were created
 * @param assistantMessage generated greeting when initialized
 */
public record ChatOnboardingInitialization(
        Boolean initialized,
        String assistantMessage
) {
    /**
     * Creates a result for an already initialized user.
     *
     * @return non-initialized result
     */
    public static ChatOnboardingInitialization existing() {
        return new ChatOnboardingInitialization(false, null);
    }
}
