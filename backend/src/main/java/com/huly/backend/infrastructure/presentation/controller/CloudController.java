package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.cloudRecommendation.CloudRecommendation;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
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

import java.util.Map;
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
            @RequestBody @Valid CloudThoughtRequest request
    ) {
        Long userId = getUserId(principal);
        String sessionId = UUID.randomUUID().toString();
        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.GUIDED_CLOUDS,
                sessionId,
                "GUIDED_CLOUD_INPUT",
                "GUIDED_CLOUD_INPUT",
                request.thought(),
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "GUIDED_CLOUDS")
        ));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recommendation")
    public ResponseEntity<CloudRecommendationResponse> getRecommendation(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid CloudRecommendationRequest request
    ) {
        CloudRecommendation recommendation = getCloudRecommendationUseCase.execute(
                request.thoughts(),
                getUserId(principal)
        );
        return ResponseEntity.ok(new CloudRecommendationResponse(
                recommendation.activityType(),
                recommendation.actionId(),
                recommendation.title(),
                recommendation.description(),
                recommendation.redirectUrl()
        ));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
