package com.huly.backend.domain.model.chat;

public record ChatStreamEvent(
        ChatStreamEventType type,
        String delta,
        ChatReply reply,
        String error
) {

    public static ChatStreamEvent delta(String delta) {
        return new ChatStreamEvent(ChatStreamEventType.DELTA, delta, null, null);
    }

    public static ChatStreamEvent metadata(ChatReply reply) {
        return new ChatStreamEvent(ChatStreamEventType.METADATA, null, reply, null);
    }

    public static ChatStreamEvent done(ChatReply reply) {
        return new ChatStreamEvent(ChatStreamEventType.DONE, null, reply, null);
    }

    public static ChatStreamEvent error(String error) {
        return new ChatStreamEvent(ChatStreamEventType.ERROR, null, null, error);
    }
}
