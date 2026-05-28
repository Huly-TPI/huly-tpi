package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.provider.VectorMemoryService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnMissingBean(VectorStore.class)
public class NoOpVectorMemoryService implements VectorMemoryService {

    @Override
    public void saveMemory(SaveVectorMemoryCommand command) {
        // Fallback para entornos sin pgvector, por ejemplo H2 o tests locales.
    }

    @Override
    public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
        // Fallback para entornos sin pgvector, por ejemplo H2 o tests locales.
        return List.of();
    }

    @Override
    public void deleteMemories(DeleteVectorMemoryCommand command) {
        // Fallback para entornos sin pgvector, por ejemplo H2 o tests locales.
    }
}
