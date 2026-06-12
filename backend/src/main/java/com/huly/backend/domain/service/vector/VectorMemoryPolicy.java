package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private final VectorMemoryProperties properties;
    private final Map<VectorMemorySource, VectorMemorySourcePolicy> sourcePolicies;
    private final DefaultVectorMemorySourcePolicy defaultSourcePolicy;

    public VectorMemoryPolicy(
            VectorMemoryProperties properties,
            List<VectorMemorySourcePolicy> sourcePolicies,
            DefaultVectorMemorySourcePolicy defaultSourcePolicy
    ) {
        this.properties = properties;
        this.sourcePolicies = sourcePolicies.stream()
                .filter(policy -> policy.sourceType() != null)
                .collect(Collectors.toMap(VectorMemorySourcePolicy::sourceType, Function.identity()));
        this.defaultSourcePolicy = defaultSourcePolicy;
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

        VectorMemorySourcePolicy sourcePolicy = sourcePolicies.getOrDefault(command.sourceType(), defaultSourcePolicy);
        int effectiveMinLength = sourcePolicy.minContentLength() >= 0
                ? sourcePolicy.minContentLength()
                : properties.getMinContentLength();

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
        if (containsAny(comparable, SENSITIVE_SIGNALS)) {
            log.info("Memoria vectorial descartada por señal sensible userId={} sourceType={}",
                    command.userId(), command.sourceType());
            return false;
        }
        return Boolean.TRUE.equals(sourcePolicy.shouldRemember(comparable));
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
