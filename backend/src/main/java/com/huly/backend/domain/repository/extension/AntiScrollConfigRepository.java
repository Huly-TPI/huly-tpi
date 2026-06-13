package com.huly.backend.domain.repository.extension;

import com.huly.backend.domain.model.extension.AntiScrollConfig;
import java.util.Optional;

public interface AntiScrollConfigRepository {
    Optional<AntiScrollConfig> findFirst();
    AntiScrollConfig save(AntiScrollConfig config);
}
