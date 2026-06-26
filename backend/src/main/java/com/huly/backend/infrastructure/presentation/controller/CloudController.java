package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloud.CreateCloudThoughtUseCase;
import com.huly.backend.domain.useCase.cloud.ListCloudThoughtsUseCase;
import com.huly.backend.domain.useCase.cloud.MarkCloudWorkedOnUseCase;
import com.huly.backend.domain.useCase.cloud.UpdateCloudStatusUseCase;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationResponse;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudThoughtRequest;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudThoughtResponse;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.UpdateCloudStatusRequest;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.cloud.CloudPresentationMapper;
import com.huly.backend.infrastructure.presentation.mapper.cloudRecommendation.CloudRecommendationPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clouds")
@RequiredArgsConstructor
public class CloudController {

    private final GetCloudRecommendationUseCase getCloudRecommendationUseCase;
    private final UserVectorMemoryService userVectorMemoryService;
    private final CreateCloudThoughtUseCase createCloudThoughtUseCase;
    private final ListCloudThoughtsUseCase listCloudThoughtsUseCase;
    private final UpdateCloudStatusUseCase updateCloudStatusUseCase;
    private final MarkCloudWorkedOnUseCase markCloudWorkedOnUseCase;
    private final CloudPresentationMapper cloudPresentationMapper;
    private final CloudRecommendationPresentationMapper cloudRecommendationPresentationMapper;

    @GetMapping
    public ResponseEntity<List<CloudThoughtResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(cloudPresentationMapper.toThoughtResponses(
                listCloudThoughtsUseCase.execute(cloudPresentationMapper.toListRequest(userId))));
    }

    @PostMapping("/thought")
    public ResponseEntity<CloudThoughtResponse> saveThought(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid CloudThoughtRequest request
    ) {
        Long userId = getUserId(principal);
        CreateCloudThoughtResponse thought = createCloudThoughtUseCase.execute(
                cloudPresentationMapper.toCreateRequest(userId, request.thought()));
        String sessionId = UUID.randomUUID().toString();
        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.GUIDED_LANTERNS,
                sessionId,
                "GUIDED_CLOUD_INPUT",
                "GUIDED_CLOUD_INPUT",
                request.thought(),
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "GUIDED_CLOUDS")
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cloudPresentationMapper.toThoughtResponse(thought));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCloudStatusRequest request) {
        Long userId = getUserId(principal);
        CloudStatus newStatus;
        try {
            newStatus = CloudStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.status());
        }
        try {
            updateCloudStatusUseCase.execute(
                    cloudPresentationMapper.toUpdateStatusRequest(id, userId, newStatus));
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/worked-on")
    public ResponseEntity<Void> markWorkedOn(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = getUserId(principal);
        markCloudWorkedOnUseCase.execute(
                cloudPresentationMapper.toMarkWorkedOnRequest(id, userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recommendation")
    public ResponseEntity<CloudRecommendationResponse> getRecommendation(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid CloudRecommendationRequest request
    ) {
        GetCloudRecommendationResponse recommendation = getCloudRecommendationUseCase.execute(
                cloudRecommendationPresentationMapper.toRecommendationRequest(
                        request.thoughts(),
                        getUserId(principal)));
        return ResponseEntity.ok(
                cloudRecommendationPresentationMapper.toRecommendationResponse(recommendation));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
