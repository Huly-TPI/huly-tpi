package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UpdateStoreItemRequest;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.service.store.StoreItemImageService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStoreItemUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;
    
    @Mock
    private StoreItemImageService imageService;
        
    private UpdateStoreItemUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new UpdateStoreItemUseCase(storeItemRepository,new StoreItemMapper(), imageService);
    }

    private StoreItem existing() {
        return StoreItem.builder()
                .id(5L)
                .assetKey("casa-rosa")
                .name("viejo")
                .description("d")
                .category(ItemCategory.HOUSE)
                .priceCoins(50)
                .imageUrl("https://x/store/light-theme/old.webp")
                .build();
    }

    private UpdateStoreItemRequest request(byte [] light, byte[] dark) {
        return new UpdateStoreItemRequest(
                5L,
                "nuevo",
                "desc2",
                ItemCategory.TREE,
                120,
                new BigDecimal("900.00"),
                true,
                light,
                dark,
                "image/webp"
        );
    }

    @Test
    void execute_shouldThrow_whenItemNotFound() {
        when(storeItemRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(request(null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test 
    void execute_shouldUploadNewImages_whenProvided() {
        when(storeItemRepository.findById(5L)).thenReturn(Optional.of(existing()));
        when(imageService.uploadThemePair(any(), any(), any())).thenReturn("http://x/light-theme/new.webp");
        when(storeItemRepository.save(any(StoreItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreItemView result = useCase.execute(request(new byte[]{1}, new byte[]{2}));

        verify(imageService).uploadThemePair(any(), any(), any());
        assertThat(result.imageUrl()).isEqualTo("http://x/light-theme/new.webp");
        assertThat(result.assetKey()).isEqualTo("casa-rosa");
        assertThat(result.name()).isEqualTo("nuevo");
        assertThat(result.category()).isEqualTo(ItemCategory.TREE);
        assertThat(result.priceCoins()).isEqualTo(120);
    }

    @Test
    void execute_shouldKeepOldImage_whenNotProvided() {
        when(storeItemRepository.findById(5L)).thenReturn(Optional.of(existing()));
        when(storeItemRepository.save(any(StoreItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreItemView result = useCase.execute(request(null, null));

        verifyNoInteractions(imageService);
        assertThat(result.imageUrl()).isEqualTo("https://x/store/light-theme/old.webp");
    }
    
}
