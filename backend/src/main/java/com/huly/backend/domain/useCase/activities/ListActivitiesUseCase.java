package com.huly.backend.domain.useCase.activities;

import lombok.RequiredArgsConstructor;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.repository.ActivityRepository;
import java.util.List;

@RequiredArgsConstructor
public class ListActivitiesUseCase {

     private final ActivityRepository activityRepository; 

      public List<Activity> execute() {
        return activityRepository.findAll();
        }
}
