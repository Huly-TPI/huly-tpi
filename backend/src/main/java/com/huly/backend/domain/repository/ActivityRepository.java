package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.Activity;
import java.util.List;

public interface ActivityRepository {
    List<Activity> findAll();
}


