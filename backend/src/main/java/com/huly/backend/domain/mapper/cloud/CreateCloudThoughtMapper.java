package com.huly.backend.domain.mapper.cloud;

import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.model.CloudThought;

/**
 * Mapper de dominio para el caso de uso de creacion de pensamiento de nube.
 */
public class CreateCloudThoughtMapper {

    public CreateCloudThoughtResponse toResponse(CloudThought thought) {
        return new CreateCloudThoughtResponse(
                thought.getId(),
                thought.getText(),
                thought.isWorkedOn(),
                thought.getCreatedAt()
        );
    }
}
