package com.huly.backend.domain.mapper.cloud;

import com.huly.backend.domain.dto.cloud.CloudThoughtItem;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsResponse;
import com.huly.backend.domain.model.CloudThought;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de pensamientos de nube.
 */
public class ListCloudThoughtsMapper {

    public ListCloudThoughtsResponse toResponse(List<CloudThought> thoughts) {
        List<CloudThoughtItem> items = thoughts.stream()
                .map(this::toItem)
                .toList();
        return new ListCloudThoughtsResponse(items);
    }

    private CloudThoughtItem toItem(CloudThought thought) {
        return new CloudThoughtItem(
                thought.getId(),
                thought.getText(),
                thought.isWorkedOn(),
                thought.getCreatedAt()
        );
    }
}
