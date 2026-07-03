package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
public class StoreItemMapperTest {

    private final StoreItemMapper mapper = new StoreItemMapper();

    @Test
    void toView_shouldMapAllFields() {
        StoreItem item = StoreItem.builder()
                .id(3L).name("n").description("d").category(ItemCategory.MANDALA)
                .assetKey("k").priceCoins(10).price(new BigDecimal("1.00"))
                .premiumOnly(true).imageUrl("http://x/light-theme/u.webp").build();

        StoreItemView view = mapper.toView(item);

        assertThat(view.id()).isEqualTo(3L);
        assertThat(view.name()).isEqualTo("n");
        assertThat(view.description()).isEqualTo("d");
        assertThat(view.category()).isEqualTo(ItemCategory.MANDALA);
        assertThat(view.assetKey()).isEqualTo("k");
        assertThat(view.priceCoins()).isEqualTo(10);
        assertThat(view.price()).isEqualByComparingTo("1.00");
        assertThat(view.premiumOnly()).isTrue();
        assertThat(view.imageUrl()).isEqualTo("http://x/light-theme/u.webp");
    }
    
}
