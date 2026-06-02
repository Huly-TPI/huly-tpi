package com.huly.backend.domain.useCase.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: crear una entrada de diario emocional y recordarla en la memoria vectorial.
 * El contenido del diario puede ser texto plano o un JSON estructurado con campos predefinidos
 * (adentro, pensamiento, bien, manana).
 *
 * El indexado vectorial permite que el chatbot recuerde lo que el usuario escribió
 * y lo use como contexto en conversaciones futuras, dando continuidad a la experiencia.
 */
@Service
@RequiredArgsConstructor
public class CreateJournalEntryUseCase {

    // ObjectMapper compartido (thread-safe) para parsear el contenido JSON del diario
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JournalEntryRepository journalEntryRepository;
    private final UserVectorMemoryService userVectorMemoryService;

    public JournalEntry execute(Long userId, String content, Mood mood, boolean useTextForAI) {
        // 1. Persiste la entrada en BD tal cual (incluyendo el JSON crudo)
        JournalEntry entry = journalEntryRepository.save(userId, content, mood);
        // 2. Construye un texto legible para la IA y lo indexa en memoria vectorial
        userVectorMemoryService.rememberJournalEntry(userId, entry.getId(), buildVectorContent(content, mood, useTextForAI));
        return entry;
    }

    /**
     * Construye el texto que se indexará en memoria vectorial para la IA.
     * Si useTextForAI=false, solo se incluye el mood (respeta la privacidad del usuario).
     * Si el contenido es JSON estructurado, extrae cada campo con su etiqueta en lenguaje natural.
     * Si el JSON falla, usa el texto crudo como fallback.
     */
    private String buildVectorContent(String content, Mood mood, boolean useTextForAI) {
        StringBuilder sb = new StringBuilder("Entrada de diario emocional.");
        if (mood != null) {
            sb.append(" Estado de ánimo: ").append(mood.name()).append(".");
        }
        if (!useTextForAI) {
            return sb.toString(); // El usuario no quiere que su texto sea analizado por la IA
        }
        try {
            // Intenta parsear el contenido como JSON estructurado del diario
            JsonNode node = MAPPER.readTree(content);
            appendField(sb, "Lo que pasa adentro", node.path("adentro").asText(null));
            appendField(sb, "Un pensamiento que quiero soltar", node.path("pensamiento").asText(null));
            appendField(sb, "Algo que me salió bien hoy", node.path("bien").asText(null));
            appendField(sb, "Lo que quiero para mañana", node.path("manana").asText(null));
        } catch (Exception e) {
            // Si no es JSON (texto libre), usa el contenido directamente
            sb.append(" ").append(content);
        }
        return sb.toString();
    }

    private void appendField(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(" ").append(label).append(": ").append(value).append(".");
        }
    }
}
