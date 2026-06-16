package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationResponse;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudThoughtRequest;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clouds")
@RequiredArgsConstructor
public class CloudController {

    private final GetCloudRecommendationUseCase getCloudRecommendationUseCase;
    private final UserVectorMemoryService userVectorMemoryService;

    @PostMapping("/thought")
    public ResponseEntity<Void> saveThought(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid CloudThoughtRequest request) {
        Long userId = getUserId(principal);
        userVectorMemoryService.rememberGuidedCloudInput(userId, UUID.randomUUID().toString(), request.thought());
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }

    @PostMapping("/recommendation")
    public ResponseEntity<CloudRecommendationResponse> getRecommendation(
            @RequestBody @Valid CloudRecommendationRequest request
    ) {
        CloudRecommendation recommendation = getCloudRecommendationUseCase.execute(request.thoughts());
        return ResponseEntity.ok(new CloudRecommendationResponse(
                recommendation.activityType(),
                recommendation.actionId(),
                recommendation.title(),
                recommendation.description(),
                recommendation.redirectUrl()
        ));
    }
}
