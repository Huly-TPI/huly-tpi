package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;

import java.util.List;

public interface VectorMemoryService {

    void saveMemory(SaveVectorMemoryCommand command);

    List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query);

    void deleteMemories(DeleteVectorMemoryCommand command);
}
