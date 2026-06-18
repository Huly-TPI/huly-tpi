package com.huly.backend.domain.repository.extension;

import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import java.util.Optional;

public interface UserAntiScrollSettingsRepository {
    Optional<UserAntiScrollSettings> findByUserId(Long userId);
    void save(Long userId, UserAntiScrollSettings settings);
}
