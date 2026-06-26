package com.huly.backend.domain.useCase.activities;

import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.mapper.activities.ListActivitiesMapper;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListActivitiesUseCase {

     private final ActivityRepository activityRepository;
     private final ListActivitiesMapper mapper;

      public ListActivitiesResponse execute() {
        return mapper.toResponse(activityRepository.findAll());
        }
}
