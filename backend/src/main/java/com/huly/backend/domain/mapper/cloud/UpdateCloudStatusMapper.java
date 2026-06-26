package com.huly.backend.domain.mapper.cloud;

import com.huly.backend.domain.dto.cloud.UpdateCloudStatusResponse;
import com.huly.backend.domain.model.CloudThought;

/**
 * Mapper de dominio para el caso de uso de actualizacion de estado de nube.
 */
public class UpdateCloudStatusMapper {

    public UpdateCloudStatusResponse toResponse(CloudThought thought) {
        return new UpdateCloudStatusResponse(
                thought.getId(),
                thought.getText(),
                thought.getStatus(),
                thought.isWorkedOn(),
                thought.getCreatedAt()
        );
    }
}
