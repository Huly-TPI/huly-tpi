package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

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

    public ChatConversationPreference withPreferredNamePendingStyle(String name, Instant now) {
        return toBuilder()
                .preferredName(name)
                .onboardingStatus(ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .updatedAt(now)
                .build();
    }

    public ChatConversationPreference withCommunicationStyle(CommunicationStyle style, Instant now) {
        return toBuilder()
                .communicationStyle(style)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .updatedAt(now)
                .build();
    }

    public ChatConversationPreference updatePreferredName(String name, Instant now) {
        return toBuilder()
                .preferredName(name)
                .updatedAt(now)
                .build();
    }

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
