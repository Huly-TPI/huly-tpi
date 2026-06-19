package com.huly.backend.domain.model.chat;

/**
 * Describes whether a preference message was answered directly or must continue
 * through the regular chatbot flow.
 */
public record ChatPreferenceHandlingResult(
        ChatReply directReply,
        boolean continueConversation,
        boolean offerCommunicationStyleWhenSafe
) {
    public static ChatPreferenceHandlingResult handled(ChatReply reply) {
        return new ChatPreferenceHandlingResult(reply, false, false);
    }

    public static ChatPreferenceHandlingResult continueChat() {
        return new ChatPreferenceHandlingResult(null, true, false);
    }

    public static ChatPreferenceHandlingResult continueChatAndOfferStyle() {
        return new ChatPreferenceHandlingResult(null, true, true);
    }
}
