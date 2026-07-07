package com.huly.backend.domain.model.chat;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

public enum ChatUserIntent {
    NONE,
    ACTIVITY_RECOMMENDATION_REQUEST,
    CHALLENGE_REQUEST;

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9ñ ]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final List<String> REQUEST_MARKERS = List.of(
            "quiero", "quisiera", "necesito", "dame", "dame una", "dame un", "mandame", "mostrame",
            "proponeme", "proponerme", "sugerime", "sugiereme", "recomendame", "recomiendame",
            "me recomendas", "me recomiendas", "me sugeris", "me sugieres", "me das", "me propones",
            "podrias darme", "podrias proponerme", "podrias recomendarme", "podes darme",
            "podes proponerme", "puedes darme", "puedes proponerme", "que puedo hacer", "algo para hacer"
    );

    private static final List<String> CHALLENGE_TERMS = List.of(
            "reto", "retos", "desafio", "desafios", "challenge"
    );

    private static final List<String> ACTIVITY_TERMS = List.of(
            "actividad", "actividades", "ejercicio", "ejercicios", "practica", "practicas",
            "recomendacion", "recomendaciones", "sugerencia", "sugerencias",
            "algo para sentirme mejor", "algo que me ayude"
    );

    /**
     * Clasifica el intent explícito del usuario a partir del texto del mensaje.
     */
    public static ChatUserIntent detect(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank()) {
            return NONE;
        }
        if (isExplicitChallengeRequest(normalized)) {
            return CHALLENGE_REQUEST;
        }
        if (isExplicitActivityRecommendationRequest(normalized)) {
            return ACTIVITY_RECOMMENDATION_REQUEST;
        }
        return NONE;
    }

    /**
     * Indica si el mensaje es una respuesta canónica a un reto propuesto.
     */
    public static boolean isChallengeResponse(String message) {
        if (message == null) {
            return false;
        }
        String normalized = normalize(message);
        return "acepto este reto".equals(normalized)
                || "rechazo este reto por ahora".equals(normalized);
    }

    private static boolean isExplicitChallengeRequest(String message) {
        return containsAny(message, CHALLENGE_TERMS) && containsAny(message, REQUEST_MARKERS);
    }

    private static boolean isExplicitActivityRecommendationRequest(String message) {
        if (containsAny(message, ACTIVITY_TERMS) && containsAny(message, REQUEST_MARKERS)) {
            return true;
        }
        return containsAny(message, List.of(
                "recomendame algo", "recomiendame algo", "sugerime algo", "sugiereme algo",
                "que puedo hacer para sentirme mejor", "necesito algo para sentirme mejor",
                "algo para bajar la ansiedad", "algo para calmarme", "algo que me ayude a calmarme"
        ));
    }

    private static boolean containsAny(String message, List<String> terms) {
        return terms.stream().anyMatch(message::contains);
    }

    private static String normalize(String message) {
        if (message == null) {
            return "";
        }
        String withoutDiacritics = DIACRITICS.matcher(Normalizer.normalize(message, Normalizer.Form.NFD))
                .replaceAll("");
        String lower = withoutDiacritics.toLowerCase();
        String wordsOnly = NON_WORD.matcher(lower).replaceAll(" ");
        return MULTI_SPACE.matcher(wordsOnly).replaceAll(" ").trim();
    }
}
