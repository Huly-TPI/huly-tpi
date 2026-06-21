package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.port.VectorMemoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.ai.vectorstore.type", havingValue = "none", matchIfMissing = true)
public class NoOpVectorMemoryPort implements VectorMemoryPort {

    @Override
    public void saveMemory(SaveVectorMemoryCommand command) {
        log.debug("Memoria vectorial desactivada. No se guarda memoria para userId={}",
                command != null ? command.userId() : null);
    }

    @Override
    public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
        log.debug("Memoria vectorial desactivada. No se buscan memorias para userId={}",
                query != null ? query.userId() : null);
        return List.of();
    }

    @Override
    public List<String> findMemoryContentsByUserIdExcludingSummary(Long userId) {
        log.debug("Memoria vectorial desactivada. No se consultan contenidos para userId={}", userId);
        return List.of();
    }

    @Override
    public List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId) {
        log.debug("Memoria vectorial desactivada. No se consultan memorias para diagnostico userId={}", userId);
        return List.of();
    }

    @Override
    public void deleteMemories(DeleteVectorMemoryCommand command) {
        log.debug("Memoria vectorial desactivada. No se eliminan memorias para userId={}",
                command != null ? command.userId() : null);
    }
}
