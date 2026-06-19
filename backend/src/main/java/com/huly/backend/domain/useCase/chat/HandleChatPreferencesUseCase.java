package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class HandleChatPreferencesUseCase {

    private static final double MIN_AI_CONFIDENCE = 0.85;
    private static final int MAX_PREFERRED_NAME_LENGTH = 50;
    private static final Pattern NAME_FORMAT = Pattern.compile(
            "[\\p{L}][\\p{L}'’-]*(?:\\s+[\\p{L}][\\p{L}'’-]*){0,2}");
    private static final List<Pattern> NAME_INTENT_PATTERNS = List.of(
            pattern("(?:me\\s+)?(?:pod[eé]s|puedes)\\s+decir\\s+(.+)$"),
            pattern("(?:ahora|desde ahora|de ahora en adelante|a partir de ahora)\\s*[,;:]?\\s*"
                    + "(?:decime|dime|ll[aá]mame|puedes llamarme|pod[eé]s llamarme)\\s+(.+)$"),
            pattern("(?:cambi[aá]|cambia)\\s+mi\\s+nombre\\s+a\\s+(.+)$"),
            pattern("(?:prefiero|quiero|me gustar[ií]a)\\s+que\\s+me\\s+(?:digas|llames)\\s+(.+)$"),
            pattern("(?:puedes|pod[eé]s)\\s+llamarme\\s+(.+)$"),
            pattern("(?:ll[aá]mame|decime|dime)\\s+(.+)$"),
            pattern("(?:mi\\s+nombre\\s+es|me\\s+llamo)\\s+([^,;.!?]+)")
    );
    private static final Set<String> INVALID_NAMES = Set.of(
            "algo", "como", "cuando", "donde", "hola", "nada", "porque", "que", "quien", "todo",
            "boa", "buen", "buena", "buenas", "buenos", "dia", "dias", "tarde", "tardes", "noche",
            "noches", "gracias", "bien", "tranqui", "chill");
    private static final List<String> STYLE_INTENT_SIGNALS = List.of(
            "hablame", "quiero que seas", "quiero que me hables", "prefiero que seas", "prefiero que me hables",
            "respondeme", "se mas", "no seas tan", "baja un poco el tono", "cambia el tono");

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final ChatPreferenceExtractionPort extractionPort;
    private final InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatQuotaService chatQuotaService;
    private final ChatConfigRepository chatConfigRepository;

    @Transactional
    public ChatPreferenceHandlingResult execute(
            Long userId,
            String conversationId,
            String message) {
        Optional<ChatConversationPreference> storedPreference =
                preferenceRepository.findByUserId(userId);
        if (storedPreference.isEmpty()) {
            chatQuotaService.assertWithinLimit(userId);
            ChatOnboardingInitialization initialization =
                    initializeChatPreferencesUseCase.execute(userId, conversationId);
            return Boolean.TRUE.equals(initialization.initialized())
                    ? ChatPreferenceHandlingResult.handled(
                            ChatReply.of(initialization.assistantMessage()))
                    : ChatPreferenceHandlingResult.continueChat();
        }

        ChatConversationPreference preference = storedPreference.get();
        return switch (preference.getOnboardingStatus()) {
            case ASKED_PREFERRED_NAME ->
                    handleAskedPreferredName(preference, conversationId, message);
            case PENDING_COMMUNICATION_STYLE ->
                    handlePendingCommunicationStyle(preference, conversationId, message);
            case ASKED_COMMUNICATION_STYLE ->
                    handleAskedCommunicationStyle(preference, conversationId, message);
            case COMPLETED ->
                    handleCompletedPreferenceChange(preference, conversationId, message);
        };
    }

    private ChatPreferenceHandlingResult handleAskedPreferredName(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.PREFERRED_NAME);
        if (!detection.hasPreference()) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }

        Instant now = Instant.now();
        ChatConversationPreference updated = preference;
        boolean hasPreferredName = detection.preferredName() != null;
        boolean hasCommunicationStyle = detection.communicationStyle() != null;
        boolean mixedMessage = detection.messageType() == ChatPreferenceMessageType.MIXED;
        boolean preferenceOnlyMessage = detection.messageType() == ChatPreferenceMessageType.PREFERENCE_ONLY;

        if (hasPreferredName) {
            updated = updated.withPreferredNamePendingStyle(detection.preferredName(), now);
        }
        if (hasCommunicationStyle) {
            updated = updated.withCommunicationStyle(detection.communicationStyle(), now);
        } else if (!communicationStyleQuestionEnabled()) {
            updated = updated.complete(now);
        } else if (preferenceOnlyMessage) {
            updated = updated.markCommunicationStyleAsked(now);
        }
        preferenceRepository.save(updated);

        if (mixedMessage) {
            return updated.getOnboardingStatus() == ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE
                    ? ChatPreferenceHandlingResult.continueChatAndOfferStyle()
                    : ChatPreferenceHandlingResult.continueChat();
        }

        String response = buildPreferredNameResponse(detection, updated);
        return directReply(preference.getUserId(), conversationId, message, response);
    }

    private ChatPreferenceHandlingResult handlePendingCommunicationStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.ANY);
        if (detection.communicationStyle() == null) {
            return ChatPreferenceHandlingResult.continueChatAndOfferStyle();
        }
        return handleDetectedCommunicationStyle(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult handleAskedCommunicationStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.COMMUNICATION_STYLE);
        if (detection.communicationStyle() == null) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }
        return handleDetectedCommunicationStyle(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult handleDetectedCommunicationStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message,
            ChatPreferenceDetectionResult detection) {
        CommunicationStyle style = detection.communicationStyle();
        preferenceRepository.save(preference.withCommunicationStyle(style, Instant.now()));
        if (detection.messageType() == ChatPreferenceMessageType.MIXED) {
            return ChatPreferenceHandlingResult.continueChat();
        }
        String response = "Entendido"
                + preferredNameSuffix(preference)
                + ". Desde ahora voy a hablarte con un estilo " + style.displayName() + ".";
        return directReply(preference.getUserId(), conversationId, message, response);
    }

    private ChatPreferenceHandlingResult handleCompletedPreferenceChange(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.ANY);
        if (!detection.hasPreference()) {
            return ChatPreferenceHandlingResult.continueChat();
        }

        ChatConversationPreference updated = preference;
        Instant now = Instant.now();
        boolean hasPreferredName = detection.preferredName() != null;
        boolean hasCommunicationStyle = detection.communicationStyle() != null;
        boolean mixedMessage = detection.messageType() == ChatPreferenceMessageType.MIXED;

        if (hasPreferredName) {
            updated = updated.updatePreferredName(detection.preferredName(), now);
        }
        if (hasCommunicationStyle) {
            updated = updated.updateCommunicationStyle(detection.communicationStyle(), now);
        }
        preferenceRepository.save(updated);

        if (mixedMessage) {
            return ChatPreferenceHandlingResult.continueChat();
        }
        return directReply(
                preference.getUserId(),
                conversationId,
                message,
                buildPreferenceChangeResponse(detection));
    }

    private ChatPreferenceHandlingResult directReply(
            Long userId,
            String conversationId,
            String userMessage,
            String assistantMessage) {
        chatQuotaService.assertWithinLimit(userId);
        saveExchange(userId, conversationId, userMessage, assistantMessage);
        return ChatPreferenceHandlingResult.handled(ChatReply.of(assistantMessage));
    }

    private String buildPreferredNameResponse(
            ChatPreferenceDetectionResult detection,
            ChatConversationPreference preference) {
        if (detection.preferredName() == null) {
            return "Entendido. Desde ahora voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        String prefix = "Perfecto, " + detection.preferredName() + ".";
        if (detection.communicationStyle() != null) {
            return prefix + " Desde ahora voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        if (preference.getOnboardingStatus() == ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE) {
            return prefix + " " + CommunicationStyle.QUESTION_TEXT;
        }
        return prefix;
    }

    private String buildPreferenceChangeResponse(ChatPreferenceDetectionResult detection) {
        if (detection.preferredName() != null && detection.communicationStyle() != null) {
            return "Listo, de ahora en adelante te voy a decir " + detection.preferredName()
                    + " y voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        if (detection.preferredName() != null) {
            return "Listo, de ahora en adelante te voy a decir "
                    + detection.preferredName() + ".";
        }
        return "Entendido. Desde ahora voy a hablarte con un estilo "
                + detection.communicationStyle().displayName() + ".";
    }

    private String preferredNameSuffix(ChatConversationPreference preference) {
        String preferredName = preference.getPreferredName();
        return preferredName == null || preferredName.isBlank() ? "" : ", " + preferredName;
    }

    private boolean communicationStyleQuestionEnabled() {
        return chatConfigRepository.findFirst()
                .map(config -> config.getCommunicationStyleQuestionEnabled() == null
                        || config.getCommunicationStyleQuestionEnabled())
                .orElse(true);
    }

    private void saveExchange(
            Long userId,
            String conversationId,
            String userMessage,
            String assistantMessage) {
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.USER, userMessage),
                userId);
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.ASSISTANT, assistantMessage),
                userId);
    }

    // --- Extracted logic from ChatPreferenceResolutionService and ChatPreferenceDetectionService ---

    private ChatPreferenceDetectionResult resolvePreference(
            String message,
            ChatPreferenceExpectedField expectedField) {
        if (expectedField != ChatPreferenceExpectedField.ANY) {
            ChatPreferenceDetectionResult semanticResult;
            try {
                semanticResult = extractionPort.extract(message, expectedField);
            } catch (RuntimeException ignored) {
                return ChatPreferenceDetectionResult.unrelated();
            }
            return validateSemanticResult(semanticResult);
        }

        if (hasPreferenceChangeSignal(message)) {
            ChatPreferenceDetectionResult semanticResult;
            try {
                semanticResult = extractionPort.extract(message, ChatPreferenceExpectedField.ANY);
            } catch (RuntimeException ignored) {
                return ChatPreferenceDetectionResult.unrelated();
            }
            return validateSemanticResult(semanticResult);
        }

        return ChatPreferenceDetectionResult.unrelated();
    }

    private boolean hasPreferenceChangeSignal(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = normalizeResolution(message);
        return normalized.contains("decime")
                || normalized.contains("dime")
                || normalized.contains("llamame")
                || normalized.contains("nombre")
                || normalized.contains("apodo")
                || normalized.contains("cambia mi")
                || normalized.contains("cambiar mi")
                || normalized.contains("hablame")
                || normalized.contains("tono")
                || normalized.contains("estilo")
                || normalized.contains("respondeme")
                || normalized.contains("seas")
                || normalized.contains("se mas")
                || normalized.contains("se menos")
                || normalized.contains("cambia el");
    }

    private ChatPreferenceDetectionResult validateSemanticResult(
            ChatPreferenceDetectionResult semanticResult) {
        if (semanticResult == null || semanticResult.confidence() < MIN_AI_CONFIDENCE) {
            return ChatPreferenceDetectionResult.unrelated();
        }

        String preferredName = validatePreferredName(semanticResult.preferredName())
                .orElse(null);
        CommunicationStyle style = semanticResult.communicationStyle();
        if (preferredName == null && style == null) {
            return ChatPreferenceDetectionResult.unrelated();
        }
        ChatPreferenceMessageType type = semanticResult.messageType() != null
                ? semanticResult.messageType()
                : ChatPreferenceMessageType.MIXED;
        return new ChatPreferenceDetectionResult(
                preferredName,
                style,
                type,
                semanticResult.confidence());
    }

    private String normalizeResolution(String value) {
        String compact = value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    // --- Detection Logic ---

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
        if (containsSentencePunctuation(compact)) {
            return Optional.empty();
        }
        return sanitizeName(compact);
    }

    public Optional<CommunicationStyle> detectCommunicationStyle(String message, Boolean onboarding) {
        String normalized = normalizeDetection(message);
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
        if (normalized.contains("indirect")) {
            return Optional.of(CommunicationStyle.INDIRECT);
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
        if (normalized.contains("formal")) {
            return Optional.of(CommunicationStyle.FORMAL);
        }
        if (normalized.contains("seri")) {
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

        String normalizedCandidate = normalizeDetection(candidate);
        String firstWord = normalizedCandidate.split("\\s+")[0];
        if (INVALID_NAMES.contains(normalizedCandidate) || INVALID_NAMES.contains(firstWord)) {
            return Optional.empty();
        }
        return Optional.of(toDisplayName(candidate));
    }

    private Optional<String> validatePreferredName(String candidate) {
        return sanitizeName(candidate);
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

    private String normalizeDetection(String value) {
        String compact = compact(value).toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private boolean containsSentencePunctuation(String value) {
        return value.indexOf(',') >= 0
                || value.indexOf(';') >= 0
                || value.indexOf(':') >= 0
                || value.indexOf('.') >= 0
                || value.indexOf('?') >= 0
                || value.indexOf('!') >= 0;
    }

    private static Pattern pattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
