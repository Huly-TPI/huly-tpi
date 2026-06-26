package com.huly.backend.infrastructure.presentation.mapper.cloud;

import com.huly.backend.domain.dto.cloud.CloudThoughtItem;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtRequest;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsRequest;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsResponse;
import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnRequest;
import com.huly.backend.domain.dto.cloud.UpdateCloudStatusRequest;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.infrastructure.presentation.dto.cloudRecommendation.CloudThoughtResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de nubes:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class CloudPresentationMapper {

    public ListCloudThoughtsRequest toListRequest(Long userId) {
        return new ListCloudThoughtsRequest(userId);
    }

    public List<CloudThoughtResponse> toThoughtResponses(ListCloudThoughtsResponse response) {
        return response.thoughts().stream()
                .map(this::toThoughtResponse)
                .toList();
    }

    public CreateCloudThoughtRequest toCreateRequest(Long userId, String text) {
        return new CreateCloudThoughtRequest(userId, text);
    }

    public CloudThoughtResponse toThoughtResponse(CreateCloudThoughtResponse response) {
        return new CloudThoughtResponse(
                response.id(),
                response.text(),
                response.workedOn(),
                response.createdAt()
        );
    }

    public UpdateCloudStatusRequest toUpdateStatusRequest(Long id, Long userId, CloudStatus newStatus) {
        return new UpdateCloudStatusRequest(id, userId, newStatus);
    }

    public MarkCloudWorkedOnRequest toMarkWorkedOnRequest(Long id, Long userId) {
        return new MarkCloudWorkedOnRequest(id, userId);
    }

    private CloudThoughtResponse toThoughtResponse(CloudThoughtItem item) {
        return new CloudThoughtResponse(
                item.id(),
                item.text(),
                item.workedOn(),
                item.createdAt()
        );
    }
}
