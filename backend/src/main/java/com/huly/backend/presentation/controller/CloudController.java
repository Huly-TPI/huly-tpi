package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.useCase.GetCloudRecommendationUseCase;
import com.huly.backend.presentation.dto.CloudRecommendationRequest;
import com.huly.backend.presentation.dto.CloudRecommendationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clouds")
public class CloudController {

    private final GetCloudRecommendationUseCase getCloudRecommendationUseCase;

    public CloudController(GetCloudRecommendationUseCase getCloudRecommendationUseCase) {
        this.getCloudRecommendationUseCase = getCloudRecommendationUseCase;
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
