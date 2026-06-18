package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Component
public class VectorMemoryPolicy {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}¿¡]");

    private static final List<String> TRIVIAL_MESSAGES = List.of(
            "hola", "holi", "buenas", "buen dia", "buenos dias", "buenas tardes", "buenas noches",
            "ok", "okay", "dale", "gracias", "si", "sí", "no", "jaja", "jajaja", "jeje", "jejeje", "entendido", "perfecto"
    );

    private static final List<String> SENSITIVE_SIGNALS = List.of(
            "diagnostico", "diagnóstico", "diagnosticado", "diagnosticada", "medicacion",
            "medicación", "medicamento", "enfermedad", "suicid", "autoles", "abuso",
            "violencia", "trauma"
    );

    private static final List<String> CHATBOT_MEMORY_SIGNALS = List.of(
            "me llamo", "mi nombre", "soy ", "tengo ", "vivo ", "trabajo", "estudio",
            "me gusta", "me encanta", "no me gusta", "prefiero", "no prefiero",
            "me relaja", "me ayuda", "me sirve", "me calma", "me cuesta", "se me dificulta",
            "suelo", "normalmente", "cuando estoy", "me siento", "estoy", "estoy estresado",
            "estoy ansioso", "me da ansiedad", "me cuesta dormir", "no puedo dormir",
            "mi amigo", "mi amiga", "mi mejor amigo", "mi mejor amiga", "mi familia",
            "quiero mejorar", "necesito", "mi objetivo",
            "usuario se llama", "usuario tiene", "usuario es estudiante"
    );

    private final VectorMemoryProperties properties;

    public VectorMemoryPolicy(VectorMemoryProperties properties) {
        this.properties = properties;
    }

    public String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        String normalized = MULTIPLE_SPACES.matcher(content.trim()).replaceAll(" ");
        if (normalized.length() > properties.getMaxContentLength()) {
            return normalized.substring(0, properties.getMaxContentLength()).trim();
        }
        return normalized;
    }

    public Boolean shouldRemember(SaveVectorMemoryCommand command, String content) {
        String normalized = normalizeContent(content);

        int effectiveMinLength = properties.getMinContentLength();

        if (normalized.length() < effectiveMinLength) {
            log.info("Memoria vectorial descartada por longitud insuficiente ({} < {}) userId={} sourceType={}",
                    normalized.length(), effectiveMinLength, command.userId(), command.sourceType());
            return false;
        }

        String comparable = comparableText(normalized);
        if (TRIVIAL_MESSAGES.contains(comparable)) {
            log.info("Memoria vectorial descartada por mensaje trivial userId={} sourceType={}",
                    command.userId(), command.sourceType());
            return false;
        }
        if (command.sourceType() == VectorMemorySource.CHATBOT && command.contentType() != null && !"CHAT_MESSAGE".equals(command.contentType())) {
            return true;
        }
        if (containsAny(comparable, SENSITIVE_SIGNALS)) {
            log.info("Memoria vectorial descartada por señal sensible userId={} sourceType={}",
                    command.userId(), command.sourceType());
            return false;
        }
        
        if (command.sourceType() == VectorMemorySource.CHATBOT) {
            return CHATBOT_MEMORY_SIGNALS.stream().anyMatch(comparable::contains);
        }
        return true;
    }

    public void validateSaveCommand(SaveVectorMemoryCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Vector memory command is required");
        }
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (command.sourceType() == null) {
            throw new IllegalArgumentException("sourceType is required");
        }
        if (normalizeContent(command.content()).isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
    }

    public String validateAndNormalizeQuery(SearchVectorMemoryQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Vector memory query is required");
        }
        if (query.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (query.sourceType() == null) {
            throw new IllegalArgumentException("sourceType is required");
        }
        if (query.limit() == null || query.limit() < 1 || query.limit() > properties.getMaxLimit()) {
            throw new IllegalArgumentException("limit must be between 1 and " + properties.getMaxLimit());
        }
        if (query.similarityThreshold() == null || query.similarityThreshold() < 0 || query.similarityThreshold() > 1) {
            throw new IllegalArgumentException("similarityThreshold must be between 0 and 1");
        }

        String normalized = normalizeContent(query.query());
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        return normalized;
    }

    private Boolean containsAny(String text, List<String> values) {
        return values.stream().anyMatch(text::contains);
    }

    private String comparableText(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        String withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return PUNCTUATION.matcher(withoutAccents).replaceAll("").trim();
    }
}
