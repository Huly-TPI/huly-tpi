package com.huly.backend.domain.model.chat;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Mensaje del usuario evaluado como posible cambio de preferencias de chat. Encapsula la
 * normalización del texto y la regla de dominio que decide si el mensaje contiene alguna
 * señal de cambio de nombre o estilo.
 */
public final class ChatPreferenceMessage {

    private static final List<String> CHANGE_SIGNALS = List.of(
            "decime", "dime", "llamame", "nombre", "apodo", "cambia mi", "cambiar mi",
            "hablame", "tono", "estilo", "respondeme", "seas", "se mas", "se menos", "cambia el");

    private final String raw;

    private ChatPreferenceMessage(String raw) {
        this.raw = raw;
    }

    public static ChatPreferenceMessage of(String raw) {
        return new ChatPreferenceMessage(raw);
    }

    /**
     * Indica si el mensaje contiene alguna señal explícita de cambio de preferencia.
     */
    public boolean hasPreferenceChangeSignal() {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String normalized = normalized();
        return CHANGE_SIGNALS.stream().anyMatch(normalized::contains);
    }

    private String normalized() {
        String compact = raw.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
