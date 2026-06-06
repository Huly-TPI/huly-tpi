package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/activities")

public class ActivityController {
    
    private final ListActivitiesUseCase listActivitiesUseCase;

     @GetMapping
     public ResponseEntity<List<Activity>> getActivities() {
        return ResponseEntity.ok(listActivitiesUseCase.execute());
}
}
