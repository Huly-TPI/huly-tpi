package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpVectorMemoryPortTest {

    private final NoOpVectorMemoryPort service = new NoOpVectorMemoryPort();

    @Test
    void saveMemory_shouldDoNothing() {
        service.saveMemory(new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "me gusta jugar a la play",
                "conv-1",
                null,
                null
        ));
    }

    @Test
    void findRelevantMemories_shouldReturnEmptyList() {
        List<?> result = service.findRelevantMemories(new SearchVectorMemoryQuery(
                1L,
                VectorMemorySource.CHATBOT,
                "consulta",
                5,
                0.65
        ));

        assertThat(result).isEmpty();
    }

    @Test
    void deleteMemories_shouldDoNothing() {
        service.deleteMemories(new DeleteVectorMemoryCommand(1L, VectorMemorySource.CHATBOT, "conv-1"));
    }
}
