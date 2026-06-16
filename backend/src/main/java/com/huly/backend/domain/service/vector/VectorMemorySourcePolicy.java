package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.VectorMemorySource;

public interface VectorMemorySourcePolicy {

    VectorMemorySource sourceType();

    Boolean shouldRemember(String normalizedContent);

    default int minContentLength() {
        return -1;
    }
}
