package com.huly.backend.infrastructure.presentation.mapper.cloudRecommendation;

import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationRequest;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudRecommendationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de recomendacion de nubes:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class CloudRecommendationPresentationMapper {

    public GetCloudRecommendationRequest toRecommendationRequest(List<String> thoughts, Long userId) {
        return new GetCloudRecommendationRequest(thoughts, userId);
    }

    public CloudRecommendationResponse toRecommendationResponse(GetCloudRecommendationResponse response) {
        return new CloudRecommendationResponse(
                response.activityType(),
                response.actionId(),
                response.title(),
                response.description(),
                response.redirectUrl()
        );
    }
}
