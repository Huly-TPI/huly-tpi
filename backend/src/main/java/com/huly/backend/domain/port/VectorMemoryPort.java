package com.huly.backend.domain.port;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface VectorMemoryPort {

    void saveMemory(SaveVectorMemoryCommand command);

    List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query);

    default List<VectorMemory> findRelevantMemories(SearchVectorMemoriesQuery query) {
        if (query == null || query.sourceTypes() == null || query.sourceTypes().isEmpty()) {
            throw new IllegalArgumentException("sourceTypes are required");
        }

        List<VectorMemory> memories = query.sourceTypes().stream()
                .filter(Objects::nonNull)
                .flatMap(sourceType -> findRelevantMemories(new SearchVectorMemoryQuery(
                        query.userId(),
                        sourceType,
                        query.query(),
                        query.limit(),
                        query.similarityThreshold()
                )).stream())
                .sorted(Comparator.comparing(VectorMemory::score, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        if (query.limit() == null) {
            return memories;
        }

        return memories.stream()
                .limit(query.limit())
                .toList();
    }

    void deleteMemories(DeleteVectorMemoryCommand command);
}
