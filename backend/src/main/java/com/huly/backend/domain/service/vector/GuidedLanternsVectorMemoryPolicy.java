package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.springframework.stereotype.Component;

@Component
public class GuidedLanternsVectorMemoryPolicy implements VectorMemorySourcePolicy {

    @Override
    public VectorMemorySource sourceType() {
        return VectorMemorySource.GUIDED_LANTERNS;
    }

    @Override
    public Boolean shouldRemember(String normalizedContent) {
        return true;
    }

    @Override
    public int minContentLength() {
        return 2;
    }
}
