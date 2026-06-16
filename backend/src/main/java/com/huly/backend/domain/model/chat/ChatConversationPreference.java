package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Stores deterministic conversational preferences for one user.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationPreference {

    private Long id;
    private Long userId;
    private String preferredName;
    private CommunicationStyle communicationStyle;
    private ChatOnboardingStatus onboardingStatus;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Creates the initial preference state for a user.
     *
     * @param userId authenticated user identifier
     * @param now creation timestamp
     * @return initial preference state
     */
    public static ChatConversationPreference begin(Long userId, Instant now) {
        return begin(userId, ChatOnboardingStatus.ASKED_PREFERRED_NAME, now);
    }

    public static ChatConversationPreference begin(
            Long userId,
            ChatOnboardingStatus initialStatus,
            Instant now) {
        return ChatConversationPreference.builder()
                .userId(userId)
                .onboardingStatus(initialStatus)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Advances onboarding after recording the preferred name.
     *
     * @param name preferred name
     * @param now update timestamp
     * @return updated preference state
     */
    public ChatConversationPreference withPreferredName(String name, Instant now) {
        return toBuilder()
                .preferredName(name)
                .onboardingStatus(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE)
                .updatedAt(now)
                .build();
    }

    public ChatConversationPreference withPreferredNamePendingStyle(String name, Instant now) {
        return toBuilder()
                .preferredName(name)
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .updatedAt(now)
                .build();
    }

    /**
     * Completes onboarding after recording the communication style.
     *
     * @param style communication style
     * @param now update timestamp
     * @return updated preference state
     */
    public ChatConversationPreference withCommunicationStyle(CommunicationStyle style, Instant now) {
        return toBuilder()
                .communicationStyle(style)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates the preferred name without changing onboarding state.
     *
     * @param name new preferred name
     * @param now update timestamp
     * @return updated preference state
     */
    public ChatConversationPreference updatePreferredName(String name, Instant now) {
        return toBuilder()
                .preferredName(name)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates the communication style without changing onboarding state.
     *
     * @param style new communication style
     * @param now update timestamp
     * @return updated preference state
     */
    public ChatConversationPreference updateCommunicationStyle(CommunicationStyle style, Instant now) {
        return toBuilder()
                .communicationStyle(style)
                .updatedAt(now)
                .build();
    }

    public ChatConversationPreference markCommunicationStyleAsked(Instant now) {
        return toBuilder()
                .onboardingStatus(ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE)
                .updatedAt(now)
                .build();
    }

    public ChatConversationPreference complete(Instant now) {
        return toBuilder()
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .updatedAt(now)
                .build();
    }

    private ChatConversationPreferenceBuilder toBuilder() {
        return ChatConversationPreference.builder()
                .id(id)
                .userId(userId)
                .preferredName(preferredName)
                .communicationStyle(communicationStyle)
                .onboardingStatus(onboardingStatus)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }
}
