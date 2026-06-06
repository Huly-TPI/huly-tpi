package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationResponse;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudThoughtRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AppUserRepository appUserRepository;

    @PostMapping("/thought")
    public ResponseEntity<Void> saveThought(@RequestBody @Valid CloudThoughtRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        userVectorMemoryService.rememberGuidedCloudInput(user.getId(), UUID.randomUUID().toString(), request.thought());
        return ResponseEntity.noContent().build();
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
