package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.ActivitySession;

public interface ActivitySessionRepository {
    ActivitySession save(ActivitySession session);
}
