package com.huly.backend.domain.repository.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import java.util.Optional;

public interface ExtensionSettingsRepository {
    Optional<ExtensionSettings> findByUserId(Long userId);
    void save(Long userId, ExtensionSettings settings);
}
