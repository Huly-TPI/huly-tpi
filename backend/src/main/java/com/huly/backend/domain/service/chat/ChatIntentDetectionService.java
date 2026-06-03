package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatUserIntent;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatIntentDetectionService {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9ñ ]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final List<String> REQUEST_MARKERS = List.of(
            "quiero",
            "quisiera",
            "necesito",
            "dame",
            "dame una",
            "dame un",
            "mandame",
            "mostrame",
            "proponeme",
            "proponerme",
            "sugerime",
            "sugiereme",
            "recomendame",
            "recomiendame",
            "me recomendas",
            "me recomiendas",
            "me sugeris",
            "me sugieres",
            "me das",
            "me propones",
            "podrias darme",
            "podrias proponerme",
            "podrias recomendarme",
            "podes darme",
            "podes proponerme",
            "puedes darme",
            "puedes proponerme",
            "que puedo hacer",
            "algo para hacer"
    );

    private static final List<String> CHALLENGE_TERMS = List.of(
            "reto",
            "retos",
            "desafio",
            "desafios",
            "challenge"
    );

    private static final List<String> ACTIVITY_TERMS = List.of(
            "actividad",
            "actividades",
            "ejercicio",
            "ejercicios",
            "practica",
            "practicas",
            "recomendacion",
            "recomendaciones",
            "sugerencia",
            "sugerencias",
            "algo para sentirme mejor",
            "algo que me ayude"
    );

    public ChatUserIntent detect(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank()) {
            return ChatUserIntent.NONE;
        }

        if (isExplicitChallengeRequest(normalized)) {
            return ChatUserIntent.CHALLENGE_REQUEST;
        }
        if (isExplicitActivityRecommendationRequest(normalized)) {
            return ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST;
        }
        return ChatUserIntent.NONE;
    }

    private boolean isExplicitChallengeRequest(String message) {
        return containsAny(message, CHALLENGE_TERMS) && containsAny(message, REQUEST_MARKERS);
    }

    private boolean isExplicitActivityRecommendationRequest(String message) {
        if (containsAny(message, ACTIVITY_TERMS) && containsAny(message, REQUEST_MARKERS)) {
            return true;
        }
        return containsAny(message, List.of(
                "recomendame algo",
                "recomiendame algo",
                "sugerime algo",
                "sugiereme algo",
                "que puedo hacer para sentirme mejor",
                "necesito algo para sentirme mejor",
                "algo para bajar la ansiedad",
                "algo para calmarme",
                "algo que me ayude a calmarme"
        ));
    }

    private boolean containsAny(String message, List<String> terms) {
        return terms.stream().anyMatch(message::contains);
    }

    private String normalize(String message) {
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
