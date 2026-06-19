package com.huly.backend.domain.repository.extension;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import java.util.Optional;

public interface AntiScrollGlobalConfigRepository {
    Optional<AntiScrollGlobalConfig> findFirst();
    AntiScrollGlobalConfig save(AntiScrollGlobalConfig config);
}
