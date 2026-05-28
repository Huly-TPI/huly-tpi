package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatbotVectorMemoryPolicy implements VectorMemorySourcePolicy {

    private static final List<String> MEMORY_SIGNALS = List.of(
            "me llamo", "mi nombre", "soy ", "tengo ", "vivo ", "trabajo", "estudio",
            "me gusta", "me encanta", "no me gusta", "prefiero", "no prefiero",
            "me relaja", "me ayuda", "me sirve", "me calma", "me cuesta", "se me dificulta",
            "suelo", "normalmente", "cuando estoy", "me siento", "estoy", "estoy estresado",
            "estoy ansioso", "me da ansiedad", "me cuesta dormir", "no puedo dormir",
            "mi amigo", "mi amiga", "mi mejor amigo", "mi mejor amiga", "mi familia",
            "quiero mejorar", "necesito", "mi objetivo"
    );

    @Override
    public VectorMemorySource sourceType() {
        return VectorMemorySource.CHATBOT;
    }

    @Override
    public Boolean shouldRemember(String normalizedContent) {
        return MEMORY_SIGNALS.stream().anyMatch(normalizedContent::contains);
    }
}
