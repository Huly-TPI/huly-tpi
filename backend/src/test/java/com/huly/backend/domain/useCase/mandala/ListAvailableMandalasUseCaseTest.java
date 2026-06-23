package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAvailableMandalasUseCaseTest {

    private static final Long USER_ID = 7L;

    private MandalaRepository mandalaRepository;
    private MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;
    private UserStoreItemRepository userStoreItemRepository;
    private GetCurrentMembershipUseCase getCurrentMembershipUseCase;
    private ListAvailableMandalasUseCase useCase;

    @BeforeEach
    void setUp() {
        mandalaRepository = mock(MandalaRepository.class);
        mandalaPlanEntitlementRepository = mock(MandalaPlanEntitlementRepository.class);
        userStoreItemRepository = mock(UserStoreItemRepository.class);
        getCurrentMembershipUseCase = mock(GetCurrentMembershipUseCase.class);
        useCase = new ListAvailableMandalasUseCase(
                mandalaRepository,
                mandalaPlanEntitlementRepository,
                userStoreItemRepository,
                getCurrentMembershipUseCase);

        when(mandalaRepository.findAllActiveOrderByDisplayOrder()).thenReturn(catalog());
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA)).thenReturn(List.of());
        when(getCurrentMembershipUseCase.execute(USER_ID)).thenReturn(Optional.empty());
    }

    @Test
    void execute_withoutPlanOrPurchasesReturnsOnlyFreeMandalas() {
        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02");
        assertThat(result).extracting("unlockSource")
                .containsExactly(MandalaUnlockSource.FREE, MandalaUnlockSource.FREE);
    }

    @Test
    void execute_withActivePlanReturnsSubscriptionPack() {
        when(getCurrentMembershipUseCase.execute(USER_ID)).thenReturn(Optional.of(activePlan("BASIC")));
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("BASIC"))
                .thenReturn(subscriptionPackIds());

        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02",
                        "mandala-03", "mandala-04", "mandala-05", "mandala-06", "mandala-07",
                        "mandala-08", "mandala-09", "mandala-10", "mandala-11", "mandala-12");
        assertThat(result.get(2).getUnlockSource()).isEqualTo(MandalaUnlockSource.SUBSCRIPTION);
    }

    @Test
    void execute_withExpiredPlanReturnsOnlyFreeMandalas() {
        when(getCurrentMembershipUseCase.execute(USER_ID)).thenReturn(Optional.empty());
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("BASIC"))
                .thenReturn(subscriptionPackIds());

        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02");
    }

    @Test
    void execute_withStorePurchaseReturnsPurchasedMandala() {
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of("mandala-13"));

        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02", "mandala-13");
        assertThat(result.get(2).getUnlockSource()).isEqualTo(MandalaUnlockSource.PURCHASED);
    }

    @Test
    void execute_doesNotReturnMandalaMissingFromActiveCatalogEvenIfOwned() {
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of("mandala-99"));

        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02");
    }



    @Test
    void execute_doesNotDuplicateFreeMandalaWhenAlsoInPlan() {
        when(getCurrentMembershipUseCase.execute(USER_ID)).thenReturn(Optional.of(activePlan("PREMIUM")));
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("PREMIUM"))
                .thenReturn(List.of("mandala-01", "mandala-03"));

        var result = useCase.execute(USER_ID);

        assertThat(result).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-01", "mandala-02", "mandala-03");
        assertThat(result.get(0).getUnlockSource()).isEqualTo(MandalaUnlockSource.FREE);
    }

    @Test
    void execute_withPageableReturnsRequestedSliceAndMetadata() {
        when(getCurrentMembershipUseCase.execute(USER_ID)).thenReturn(Optional.of(activePlan("BASIC")));
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("BASIC"))
                .thenReturn(subscriptionPackIds());

        var result = useCase.execute(USER_ID, PageRequest.of(1, 5));

        assertThat(result.getContent()).extracting(item -> item.getMandala().getId())
                .containsExactly("mandala-06", "mandala-07", "mandala-08", "mandala-09", "mandala-10");
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(12);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    void execute_withPageableOutsideRangeReturnsEmptyPage() {
        var result = useCase.execute(USER_ID, PageRequest.of(2, 5));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    private List<Mandala> catalog() {
        return IntStream.rangeClosed(1, 21)
                .mapToObj(index -> {
                    String id = "mandala-%02d".formatted(index);
                    if (index <= 2) {
                        return mandala(id, MandalaAccessType.FREE, null);
                    }
                    if (index <= 12) {
                        return mandala(id, MandalaAccessType.SUBSCRIPTION, null);
                    }
                    return mandala(id, MandalaAccessType.PURCHASABLE, 100);
                })
                .toList();
    }

    private List<String> subscriptionPackIds() {
        return IntStream.rangeClosed(3, 12)
                .mapToObj(index -> "mandala-%02d".formatted(index))
                .toList();
    }

    private UserStoreItem ownedMandala(String assetKey) {
        StoreItem storeItem = StoreItem.builder()
                .id(10L)
                .name("Mandala")
                .description("desc")
                .category(ItemCategory.MANDALA)
                .assetKey(assetKey)
                .priceCoins(100)
                .build();
        return UserStoreItem.builder()
                .userId(USER_ID)
                .storeItem(storeItem)
                .build();
    }

    private Mandala mandala(String id, MandalaAccessType accessType, Integer priceCoins) {
        return Mandala.builder()
                .id(id)
                .title(id)
                .description("desc")
                .assetKey(id)
                .displayOrder(Integer.parseInt(id.substring(id.length() - 2)))
                .active(true)
                .accessType(accessType)
                .priceCoins(priceCoins)
                .build();
    }

    private UserPlan activePlan(String planCode) {
        return UserPlan.builder()
                .userId(USER_ID)
                .planCode(planCode)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
