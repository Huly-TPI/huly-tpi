package com.huly.backend.domain.model.chat;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reglas de dominio para sanitizar y validar el nombre preferido que el usuario elige
 * para el chatbot. Sin I/O: solo aplica formato, longitud y una lista negra de términos
 * que no son nombres.
 */
public final class PreferredName {

    private static final int MAX_LENGTH = 50;
    private static final Pattern FORMAT = Pattern.compile(
            "[\\p{L}][\\p{L}'’-]*(?:\\s+[\\p{L}][\\p{L}'’-]*){0,2}");
    private static final Set<String> INVALID = Set.of(
            "algo", "como", "cuando", "donde", "hola", "nada", "porque", "que", "quien", "todo",
            "boa", "buen", "buena", "buenas", "buenos", "dia", "dias", "tarde", "tardes", "noche",
            "noches", "gracias", "bien", "tranqui", "chill");

    private PreferredName() {
    }

    /**
     * Devuelve el nombre preferido válido a partir de un candidato crudo, o vacío si el
     * candidato no cumple las reglas de dominio.
     */
    public static Optional<String> sanitize(String rawCandidate) {
        String candidate = compact(rawCandidate)
                .replaceFirst("(?i)\\s+por\\s+favor\\s*$", "")
                .replaceFirst("[,;.!?].*$", "")
                .replaceAll("^[\"'“”]+|[\"'“”]+$", "")
                .trim();
        if (candidate.isEmpty() || candidate.length() > MAX_LENGTH
                || !FORMAT.matcher(candidate).matches()) {
            return Optional.empty();
        }

        String normalized = normalize(candidate);
        String firstWord = normalized.split("\\s+")[0];
        if (INVALID.contains(normalized) || INVALID.contains(firstWord)) {
            return Optional.empty();
        }
        return Optional.of(toDisplayName(candidate));
    }

    private static String toDisplayName(String candidate) {
        if (!candidate.equals(candidate.toLowerCase(Locale.ROOT))) {
            return candidate;
        }
        String[] words = candidate.split("\\s+");
        for (int index = 0; index < words.length; index++) {
            words[index] = words[index].substring(0, 1).toUpperCase(Locale.ROOT)
                    + words[index].substring(1);
        }
        return String.join(" ", words);
    }

    private static String compact(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String normalize(String value) {
        String compact = compact(value).toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
