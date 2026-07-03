package com.huly.backend.infrastructure.presentation.mapper.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasRequest;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasResponse;
import com.huly.backend.domain.dto.mandala.MandalaItem;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaPageResponse;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaResponse;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaSessionStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de mandalas:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class MandalaPresentationMapper {

    public ListAvailableMandalasRequest toListRequest(Long userId, int page, int size) {
        return new ListAvailableMandalasRequest(userId, page, size);
    }

    public SaveMandalaProgressRequest toSaveRequest(Long userId, String mandalaId, byte[] paintBlob) {
        return new SaveMandalaProgressRequest(userId, mandalaId, paintBlob);
    }

    public GetMandalaProgressRequest toGetRequest(Long userId, String mandalaId) {
        return new GetMandalaProgressRequest(userId, mandalaId);
    }

    public ClearMandalaProgressRequest toClearRequest(Long userId, String mandalaId) {
        return new ClearMandalaProgressRequest(userId, mandalaId);
    }

    public GetMandalaSessionStatusRequest toStatusRequest(Long userId, String mandalaId) {
        return new GetMandalaSessionStatusRequest(userId, mandalaId);
    }

    public MandalaPageResponse toPageResponse(ListAvailableMandalasResponse response) {
        return new MandalaPageResponse(
                response.content().stream().map(this::toMandalaResponse).toList(),
                response.pageNumber(),
                response.pageSize(),
                response.totalElements(),
                response.totalPages(),
                response.first(),
                response.last()
        );
    }

    public ResponseEntity<byte[]> toProgressResponse(GetMandalaProgressResponse response) {
        return response.paintBlob()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public MandalaSessionStatusResponse toSessionStatusResponse(GetMandalaSessionStatusResponse response) {
        return new MandalaSessionStatusResponse(response.sessionRegistered());
    }

    private MandalaResponse toMandalaResponse(MandalaItem item) {
        return new MandalaResponse(
                item.id(),
                item.title(),
                item.description(),
                item.assetKey(),
                item.displayOrder(),
                item.unlockSource(),
                item.accessType(),
                item.locked()
        );
    }
}
