package com.huly.backend.domain.useCase.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateJournalEntryUseCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JournalEntryRepository journalEntryRepository;
    private final UserVectorMemoryService userVectorMemoryService;

    public JournalEntry execute(Long userId, String content, Mood mood) {
        JournalEntry entry = journalEntryRepository.save(userId, content, mood);
        userVectorMemoryService.rememberJournalEntry(userId, entry.getId(), buildVectorContent(content, mood));
        return entry;
    }

    private String buildVectorContent(String content, Mood mood) {
        StringBuilder sb = new StringBuilder("Entrada de diario emocional.");
        if (mood != null) {
            sb.append(" Estado de ánimo: ").append(mood.name()).append(".");
        }
        try {
            JsonNode node = MAPPER.readTree(content);
            appendField(sb, "Lo que pasa adentro", node.path("adentro").asText(null));
            appendField(sb, "Un pensamiento que quiero soltar", node.path("pensamiento").asText(null));
            appendField(sb, "Algo que me salió bien hoy", node.path("bien").asText(null));
            appendField(sb, "Lo que quiero para mañana", node.path("manana").asText(null));
        } catch (Exception e) {
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
