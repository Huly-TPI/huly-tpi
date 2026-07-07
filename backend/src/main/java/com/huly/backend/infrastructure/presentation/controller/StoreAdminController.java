package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.store.CreateStoreItemRequest;
import com.huly.backend.domain.dto.store.DeleteStoreItemRequest;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UpdateStoreItemRequest;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.CreateStoreItemUseCase;
import com.huly.backend.domain.useCase.store.DeleteStoreItemUseCase;
import com.huly.backend.domain.useCase.store.UpdateStoreItemUseCase;
import com.huly.backend.infrastructure.presentation.dto.store.StoreItemResponse;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/store/items")
public class StoreAdminController {

    private final CreateStoreItemUseCase createStoreItemUseCase;
    private final UpdateStoreItemUseCase updateStoreItemUseCase;
    private final DeleteStoreItemUseCase deleteStoreItemUseCase;
    private final StorePresentationMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreItemResponse> create(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam int priceCoins,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(defaultValue = "false") boolean premiumOnly,
            @RequestParam MultipartFile imageLight,
            @RequestParam MultipartFile imageDark) throws IOException {
        CreateStoreItemRequest request = new CreateStoreItemRequest(
                name, description, ItemCategory.valueOf(category), priceCoins, price, premiumOnly,
                imageLight.getBytes(), imageDark.getBytes(), imageLight.getContentType());
        StoreItemView created = createStoreItemUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toStoreItemResponse(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreItemResponse> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam int priceCoins,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(defaultValue = "false") boolean premiumOnly,
            @RequestParam(required = false) MultipartFile imageLight,
            @RequestParam(required = false) MultipartFile imageDark) throws IOException {
          byte[] light = imageLight != null ? imageLight.getBytes() : null;
        byte[] dark = imageDark != null ? imageDark.getBytes() : null;
        String contentType = imageLight != null ? imageLight.getContentType() : null;
        UpdateStoreItemRequest request = new UpdateStoreItemRequest(
                id, name, description, ItemCategory.valueOf(category), priceCoins, price, premiumOnly,
                light, dark, contentType);
        StoreItemView updated = updateStoreItemUseCase.execute(request);
        return ResponseEntity.ok(mapper.toStoreItemResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteStoreItemUseCase.execute(new DeleteStoreItemRequest(id));
        return ResponseEntity.noContent().build();
    }
}