package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.CreateStoreItemRequest;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.service.store.StoreItemImageService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateStoreItemUseCase {
    
    private final StoreItemRepository storeItemRepository;
    private final StoreItemMapper storeItemMapper;
    private final StoreItemImageService storeItemImageService;

    public StoreItemView execute(CreateStoreItemRequest request) {
        String imageUrl = storeItemImageService.uploadThemePair(
                request.imageLight(), request.imageDark(), request.imageContentType());
        StoreItem saved = storeItemRepository.save(StoreItem.builder()
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