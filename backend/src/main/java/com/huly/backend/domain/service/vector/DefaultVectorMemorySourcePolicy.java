package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.springframework.stereotype.Component;

@Component
public class DefaultVectorMemorySourcePolicy implements VectorMemorySourcePolicy {

    @Override
    public VectorMemorySource sourceType() {
        return null;
    }

    @Override
    public Boolean shouldRemember(String normalizedContent) {
        return true;
    }
}
