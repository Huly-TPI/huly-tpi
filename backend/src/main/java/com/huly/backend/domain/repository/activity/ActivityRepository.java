package com.huly.backend.domain.repository.activity;

import com.huly.backend.domain.model.activity.Activity;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository {
    List<Activity> findAll();

    boolean existsById(Long id);
    Optional<Activity> findById(Long id);
    Activity save(Activity activity);
}

