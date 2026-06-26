package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.useCase.userPlant.GetPlantGoalsUseCase;
import com.huly.backend.domain.useCase.userPlant.GetUserPlantsUseCase;
import com.huly.backend.infrastructure.presentation.dto.userPlant.PlantGoalsResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.UserPlantSummaryResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.userPlant.UserPlantPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-plants")
@RequiredArgsConstructor
public class UserPlantController {

    private final GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private final GetUserPlantsUseCase getUserPlantsUseCase;
    private final GetPlantGoalsUseCase getPlantGoalsUseCase;
    private final UserPlantPresentationMapper mapper;

    @GetMapping("/current")
    public ResponseEntity<UserPlantSummaryResponse> getCurrent(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        GetCurrentPlantResponse response =
                getOrCreateCurrentPlantUseCase.execute(mapper.toGetCurrentPlantRequest(userId));
        return ResponseEntity.ok(mapper.toSummary(response));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserPlantSummaryResponse>> getAll(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        GetUserPlantsResponse response = getUserPlantsUseCase.execute(mapper.toGetUserPlantsRequest(userId));
        return ResponseEntity.ok(mapper.toSummaries(response));
    }

    @GetMapping("/{id}/goals")
    public ResponseEntity<PlantGoalsResponse> getGoals(@PathVariable Long id) {
        GetPlantGoalsResponse response = getPlantGoalsUseCase.execute(mapper.toGetPlantGoalsRequest(id));
        return ResponseEntity.ok(mapper.toPlantGoalsResponse(response));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
