package com.huly.backend.domain.useCase.activities;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.repository.ActivityRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListActivitiesUseCase {

     private final ActivityRepository activityRepository; 

      public List<Activity> execute() {
        return activityRepository.findAll();
        }
}
