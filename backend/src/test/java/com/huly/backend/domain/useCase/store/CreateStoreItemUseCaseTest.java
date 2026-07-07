package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.CreateStoreItemRequest;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.service.store.StoreItemImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStoreItemUseCaseTest {

    private static final String IMAGE_URL = "http://x/light-theme/u.webp";

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private StoreItemImageService imageService;

    private final ArgumentCaptor<StoreItem> storeItemCaptor = ArgumentCaptor.forClass(StoreItem.class);

    private CreateStoreItemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateStoreItemUseCase(storeItemRepository, new StoreItemMapper(), imageService);
    }

    @Test
    @DisplayName("Sube las imágenes y guarda el nuevo item")
    void executeShouldUploadImagesAndSaveNewItem() {
        givenImagesUploadedAndItemSaved();

        StoreItemView result = create();

        thenNewItemSavedWithUploadedImage(result);
    }

    // --- arrange ---

    private void givenImagesUploadedAndItemSaved() {
        when(imageService.uploadThemePair(any(), any(), any())).thenReturn(IMAGE_URL);
        when(storeItemRepository.save(any(StoreItem.class))).thenReturn(saved());
    }

    private CreateStoreItemRequest request() {
        return new CreateStoreItemRequest(
                "Casa nueva",
                "desc",
                ItemCategory.HOUSE,
                80,
                new BigDecimal("500.00"),
                false,
                new byte[] { 1 },
                new byte[] { 2 },
                "image/webp");
    }

    private StoreItem saved() {
        return StoreItem.builder()
                .id(7L)
                .name("Casa nueva")
                .description("desc")
                .category(ItemCategory.HOUSE)
                .priceCoins(80)
                .price(new BigDecimal("500.00"))
                .premiumOnly(false)
                .imageUrl(IMAGE_URL)
                .build();
    }

    // --- act ---

    private StoreItemView create() {
        return useCase.execute(request());
    }

    // --- assert ---

    private void thenNewItemSavedWithUploadedImage(StoreItemView result) {
        verify(imageService).uploadThemePair(any(), any(), any());
        verify(storeItemRepository).save(storeItemCaptor.capture());
        StoreItem toSave = storeItemCaptor.getValue();
        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getAssetKey()).isNull();
        assertThat(toSave.getName()).isEqualTo("Casa nueva");
        assertThat(toSave.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
    }
}
