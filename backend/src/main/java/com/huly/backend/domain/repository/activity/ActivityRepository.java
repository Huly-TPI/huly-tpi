package com.huly.backend.domain.repository.activity;

import com.huly.backend.domain.model.activity.Activity;
import java.util.List;

public interface ActivityRepository {
    List<Activity> findAll();

    boolean existsById(Long id);
}

