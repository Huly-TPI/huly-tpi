package com.huly.backend.domain.mapper.cloudRecommendation;

import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.domain.model.cloudRecommendation.CloudRecommendation;

/**
 * Mapper de dominio para el caso de uso de recomendacion de nube.
 */
public class GetCloudRecommendationMapper {

    public GetCloudRecommendationResponse toResponse(CloudRecommendation recommendation) {
        return new GetCloudRecommendationResponse(
                recommendation.activityType(),
                recommendation.actionId(),
                recommendation.title(),
                recommendation.description(),
                recommendation.redirectUrl()
        );
    }
}
