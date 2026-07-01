package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UpdateStoreItemRequest;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.service.store.StoreItemImageService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateStoreItemUseCase {

    private final StoreItemRepository storeItemRepository;
    private final StoreItemMapper storeItemMapper;
    private final StoreItemImageService storeItemImageService;

    public StoreItemView execute(UpdateStoreItemRequest request) {
        StoreItem existing = storeItemRepository.findById(request.id())
                .orElseThrow(() -> new NotFoundException("Item no encontrado " + request.id()));

        String imageUrl = existing.getImageUrl();
        if (request.imageLight() != null) {
            imageUrl = storeItemImageService.uploadThemePair(
                    request.imageLight(), request.imageDark(), request.imageContentType());
        }

        StoreItem saved = storeItemRepository.save(StoreItem.builder()
                .id(existing.getId())
                .assetKey(existing.getAssetKey())
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .priceCoins(request.priceCoins())
                .price(request.price())
                .premiumOnly(request.premiumOnly())
                .imageUrl(imageUrl)
                .build());
        return storeItemMapper.toView(saved);
    }

}
