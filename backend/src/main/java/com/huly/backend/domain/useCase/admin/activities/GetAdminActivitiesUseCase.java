package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAdminActivitiesUseCase {

    private final ActivityRepository activityRepository;

    public List<Activity> execute() {
        return activityRepository.findAll();
    }
}
