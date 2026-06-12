package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.enums.CommunicationStyle;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects explicit conversational preference changes without relying on an LLM.
 */
@Service
public class ChatPreferenceDetectionService {

    private static final int MAX_PREFERRED_NAME_LENGTH = 50;
    private static final Pattern NAME_FORMAT = Pattern.compile(
            "[\\p{L}][\\p{L}'’-]*(?:\\s+[\\p{L}][\\p{L}'’-]*){0,2}");
    private static final List<Pattern> NAME_INTENT_PATTERNS = List.of(
            pattern("(?:ahora|desde ahora|de ahora en adelante|a partir de ahora)\\s*[,;:]?\\s*"
                    + "(?:decime|dime|ll[aá]mame|puedes llamarme|pod[eé]s llamarme)\\s+(.+)$"),
            pattern("(?:cambi[aá]|cambia)\\s+mi\\s+nombre\\s+a\\s+(.+)$"),
            pattern("(?:prefiero|quiero|me gustar[ií]a)\\s+que\\s+me\\s+(?:digas|llames)\\s+(.+)$"),
            pattern("(?:puedes|pod[eé]s)\\s+llamarme\\s+(.+)$"),
            pattern("(?:ll[aá]mame|decime|dime)\\s+(.+)$")
    );
    private static final Set<String> INVALID_NAMES = Set.of(
            "algo", "como", "cuando", "donde", "hola", "nada", "porque", "que", "quien", "todo");
    private static final List<String> STYLE_INTENT_SIGNALS = List.of(
            "hablame", "quiero que seas", "quiero que me hables", "prefiero que seas", "prefiero que me hables",
            "respondeme", "se mas", "no seas tan", "baja un poco el tono", "cambia el tono");

    /**
     * Extracts a preferred name from an onboarding answer or explicit change request.
     *
     * @param message user message
     * @param onboarding whether a short standalone answer is currently expected
     * @return validated preferred name when detected
     */
    public Optional<String> detectPreferredName(String message, Boolean onboarding) {
        String compact = compact(message);
        if (compact.isEmpty()) {
            return Optional.empty();
        }

        Optional<String> explicitName = extractExplicitName(compact);
        if (explicitName.isPresent()) {
            return explicitName;
        }
        if (!Boolean.TRUE.equals(onboarding)) {
            return Optional.empty();
        }
        return sanitizeName(compact);
    }

    /**
     * Extracts a supported communication style from an onboarding answer or explicit change request.
     *
     * @param message user message
     * @param onboarding whether a short standalone answer is currently expected
     * @return communication style when detected
     */
    public Optional<CommunicationStyle> detectCommunicationStyle(String message, Boolean onboarding) {
        String normalized = normalize(message);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        if (!Boolean.TRUE.equals(onboarding)
                && STYLE_INTENT_SIGNALS.stream().noneMatch(normalized::contains)) {
            return Optional.empty();
        }

        if (normalized.contains("no seas tan serio")
                || normalized.contains("menos serio")
                || normalized.contains("menos formal")) {
            return Optional.of(CommunicationStyle.INFORMAL);
        }
        if (normalized.contains("baja un poco el tono")
                || normalized.contains("suave")
                || normalized.contains("contenedor")
                || normalized.contains("contenedora")) {
            return Optional.of(CommunicationStyle.GENTLE_SUPPORTIVE);
        }
        if (normalized.contains("amigo de toda la vida")
                || normalized.contains("como un amigo")
                || normalized.contains("como una amiga")) {
            return Optional.of(CommunicationStyle.FRIEND_LIKE);
        }
        if (normalized.contains("corto") && normalized.contains("direct")) {
            return Optional.of(CommunicationStyle.CONCISE_DIRECT);
        }
        if (normalized.contains("direct")) {
            return Optional.of(CommunicationStyle.DIRECT);
        }
        if (normalized.contains("informal")) {
            return Optional.of(CommunicationStyle.INFORMAL);
        }
        if (normalized.contains("neutr")) {
            return Optional.of(CommunicationStyle.NEUTRAL);
        }
        if (normalized.contains("seri") || normalized.contains("formal")) {
            return Optional.of(CommunicationStyle.SERIOUS);
        }
        if (normalized.contains("amable") || normalized.contains("simpatic")) {
            return Optional.of(CommunicationStyle.FRIENDLY);
        }
        if (normalized.contains("cercan")) {
            return Optional.of(CommunicationStyle.CLOSE);
        }
        if (normalized.contains("motiv")) {
            return Optional.of(CommunicationStyle.MOTIVATIONAL);
        }
        return Optional.empty();
    }

    private Optional<String> extractExplicitName(String message) {
        for (Pattern intentPattern : NAME_INTENT_PATTERNS) {
            Matcher matcher = intentPattern.matcher(message);
            if (matcher.find()) {
                return sanitizeName(matcher.group(1));
            }
        }
        return Optional.empty();
    }

    private Optional<String> sanitizeName(String rawCandidate) {
        String candidate = compact(rawCandidate)
                .replaceFirst("(?i)\\s+por\\s+favor\\s*$", "")
                .replaceFirst("[,;.!?].*$", "")
                .replaceAll("^[\"'“”]+|[\"'“”]+$", "")
                .trim();
        if (candidate.isEmpty() || candidate.length() > MAX_PREFERRED_NAME_LENGTH
                || !NAME_FORMAT.matcher(candidate).matches()) {
            return Optional.empty();
        }

        String normalizedCandidate = normalize(candidate);
        String firstWord = normalizedCandidate.split("\\s+")[0];
        if (INVALID_NAMES.contains(normalizedCandidate) || INVALID_NAMES.contains(firstWord)) {
            return Optional.empty();
        }
        return Optional.of(toDisplayName(candidate));
    }

    private String toDisplayName(String candidate) {
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

    private String compact(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalize(String value) {
        String compact = compact(value).toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private static Pattern pattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
