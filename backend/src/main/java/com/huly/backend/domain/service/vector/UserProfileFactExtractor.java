package com.huly.backend.domain.service.vector;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserProfileFactExtractor {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "\\b(?:mi nombre es|me llamo)\\s+([A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern AGE_PATTERN = Pattern.compile(
            "\\b(?:tengo|mi edad es)\\s+(\\d{1,3})\\s*(?:años|anos|anios)?\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern STUDENT_PATTERN = Pattern.compile(
            "\\bsoy\\s+(?:un|una)?\\s*estudiante(?:\\s+de\\s+([^.,;!¡?¿]+))?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    public Optional<String> extractProfileFacts(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        List<String> facts = new ArrayList<>();
        addNameFact(message, facts);
        addAgeFact(message, facts);
        addStudentFact(message, facts);

        if (facts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(" ", facts));
    }

    public Boolean asksForProfileFact(String query) {
        String normalized = normalize(query);
        return normalized.contains("edad")
                || normalized.contains("anos")
                || normalized.contains("anios")
                || normalized.contains("nombre")
                || normalized.contains("llamo")
                || normalized.contains("quien soy")
                || normalized.contains("estudio")
                || normalized.contains("estudiante")
                || normalized.contains("datos personales")
                || normalized.contains("sabes de mi")
                || normalized.contains("saber de mi")
                || normalized.contains("sobre mi");
    }

    public String buildProfileRecallQuery(String query) {
        return normalizeForRecall(query)
                + " datos personales del usuario nombre edad anos anios años tiene tengo estudiante estudio perfil";
    }

    private void addNameFact(String message, List<String> facts) {
        Matcher matcher = NAME_PATTERN.matcher(message);
        if (matcher.find()) {
            facts.add("El usuario se llama " + capitalize(matcher.group(1)) + ".");
        }
    }

    private void addAgeFact(String message, List<String> facts) {
        Matcher matcher = AGE_PATTERN.matcher(message);
        if (matcher.find()) {
            facts.add("El usuario tiene " + matcher.group(1) + " años.");
        }
    }

    private void addStudentFact(String message, List<String> facts) {
        Matcher matcher = STUDENT_PATTERN.matcher(message);
        if (matcher.find()) {
            String field = matcher.group(1);
            if (field == null || field.isBlank()) {
                facts.add("El usuario es estudiante.");
            } else {
                facts.add("El usuario es estudiante de " + field.trim() + ".");
            }
        }
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    private String normalizeForRecall(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim();
    }
}
