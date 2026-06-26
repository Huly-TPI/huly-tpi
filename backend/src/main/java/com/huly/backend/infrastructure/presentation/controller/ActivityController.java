package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.domain.useCase.activities.RegisterActivitySessionUseCase;
import com.huly.backend.infrastructure.presentation.dto.activities.ActivityResponse;
import com.huly.backend.infrastructure.presentation.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.activities.ActivityPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ListActivitiesUseCase listActivitiesUseCase;
    private final RegisterActivitySessionUseCase registerActivitySessionUseCase;
    private final ActivityPresentationMapper activityPresentationMapper;

     @GetMapping
     public ResponseEntity<List<ActivityResponse>> getActivities() {
        return ResponseEntity.ok(activityPresentationMapper.toActivityResponses(listActivitiesUseCase.execute()));
     }

     @PostMapping("/sessions")
     public ResponseEntity<Void> registerSession(
             @AuthenticationPrincipal UserDetails principal,
             @Valid @RequestBody RegisterActivitySessionRequest request
     ) {
         if (principal == null) {
             throw new UnauthorizedException("Not authenticated");
         }

         try {
             Long userId = Long.valueOf(principal.getUsername());
             registerActivitySessionUseCase.execute(
                     activityPresentationMapper.toRegisterRequest(userId, request.getActivityType()));
         } catch (NumberFormatException e) {
             throw new UnauthorizedException("Not authenticated");
         }

         return ResponseEntity.ok().build();
     }
}
