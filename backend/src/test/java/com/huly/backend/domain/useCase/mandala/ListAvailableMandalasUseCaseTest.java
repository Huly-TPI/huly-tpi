package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ListAvailableMandalasRequest;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasResponse;
import com.huly.backend.domain.dto.mandala.MandalaItem;
import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.mapper.mandala.ListAvailableMandalasMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAvailableMandalasUseCaseTest {

    private static final Long USER_ID = 7L;

    @Mock
    private MandalaRepository mandalaRepository;

    @Mock
    private MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    @Mock
    private GetCurrentMembershipUseCase getCurrentMembershipUseCase;

    private ListAvailableMandalasUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListAvailableMandalasUseCase(
                mandalaRepository,
                mandalaPlanEntitlementRepository,
                userStoreItemRepository,
                getCurrentMembershipUseCase,
                new ListAvailableMandalasMapper());
    }

    @Test
    @DisplayName("Devuelve solo los mandalas gratuitos cuando no hay plan ni compras")
    void executeWithoutPlanOrPurchasesReturnsOnlyFreeMandalas() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenInactiveMembership();

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02");
        thenUnlockSourcesAre(result, MandalaUnlockSource.FREE, MandalaUnlockSource.FREE);
    }

    @Test
    @DisplayName("Devuelve el pack de suscripcion cuando el plan esta activo")
    void executeWithActivePlanReturnsSubscriptionPack() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenActivePlan("BASIC");
        givenSubscriptionPackEntitlements("BASIC");

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02",
                "mandala-03", "mandala-04", "mandala-05", "mandala-06", "mandala-07",
                "mandala-08", "mandala-09", "mandala-10", "mandala-11", "mandala-12");
        thenUnlockSourceAtUnlockedIndexIs(result, 2, MandalaUnlockSource.SUBSCRIPTION);
    }

    @Test
    @DisplayName("Devuelve solo los mandalas gratuitos cuando el plan esta expirado")
    void executeWithExpiredPlanReturnsOnlyFreeMandalas() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenInactiveMembership();
        givenUnusedPlanEntitlements("BASIC");

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02");
    }

    @Test
    @DisplayName("Incluye el mandala comprado en la tienda")
    void executeWithStorePurchaseReturnsPurchasedMandala() {
        // --- arrange ---
        givenActiveCatalog();
        givenStorePurchases("mandala-13");
        givenInactiveMembership();

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02", "mandala-13");
        thenUnlockSourceAtUnlockedIndexIs(result, 2, MandalaUnlockSource.PURCHASED);
    }

    @Test
    @DisplayName("No devuelve un mandala comprado que no esta en el catalogo activo")
    void executeDoesNotReturnMandalaMissingFromActiveCatalogEvenIfOwned() {
        // --- arrange ---
        givenActiveCatalog();
        givenStorePurchases("mandala-99");
        givenInactiveMembership();

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02");
    }

    @Test
    @DisplayName("No duplica un mandala gratuito que tambien esta en el plan")
    void executeDoesNotDuplicateFreeMandalaWhenAlsoInPlan() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenActivePlan("PREMIUM");
        givenPlanEntitlements("PREMIUM", List.of("mandala-01", "mandala-03"));

        // --- act ---
        List<AvailableMandala> result = listAvailableMandalas();

        // --- assert ---
        thenSizeIs(result, 21);
        thenUnlockedMandalaIdsAre(result, "mandala-01", "mandala-02", "mandala-03");
        thenUnlockSourceAtUnlockedIndexIs(result, 0, MandalaUnlockSource.FREE);
    }

    @Test
    @DisplayName("Devuelve la porcion solicitada y sus metadatos al paginar")
    void executeWithPageableReturnsRequestedSliceAndMetadata() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenActivePlan("BASIC");
        givenSubscriptionPackEntitlements("BASIC");

        // --- act ---
        ListAvailableMandalasResponse result = listAvailableMandalasPage(1, 5);

        // --- assert ---
        thenPageContentIdsAre(result, "mandala-06", "mandala-07", "mandala-08", "mandala-09", "mandala-10");
        thenPageMetadata(result, 1, 5, 21, 5, false, false);
    }

    @Test
    @DisplayName("Devuelve una pagina vacia cuando se pide fuera de rango")
    void executeWithPageableOutsideRangeReturnsEmptyPage() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenInactiveMembership();

        // --- act ---
        ListAvailableMandalasResponse result = listAvailableMandalasPage(5, 5);

        // --- assert ---
        thenPageContentIsEmpty(result);
        thenPageMetadata(result, 5, 5, 21, 5, false, true);
    }

    @Test
    @DisplayName("Devuelve el mandala restante bloqueado en la ultima pagina")
    void executeWithPageableOnLastPageReturnsRemainingLockedMandala() {
        // --- arrange ---
        givenActiveCatalog();
        givenNoStorePurchases();
        givenInactiveMembership();

        // --- act ---
        ListAvailableMandalasResponse result = listAvailableMandalasPage(4, 5);

        // --- assert ---
        thenPageContentIdsAre(result, "mandala-21");
        thenPageMetadata(result, 4, 5, 21, 5, false, true);
        thenPageItemIsLocked(result, 0);
    }

    // --- arrange ---

    private void givenActiveCatalog() {
        when(mandalaRepository.findAllActiveOrderByDisplayOrder()).thenReturn(catalog());
    }

    private void givenNoStorePurchases() {
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of());
    }

    private void givenStorePurchases(String... assetKeys) {
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of(assetKeys));
    }

    private void givenInactiveMembership() {
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(GetCurrentMembershipResponse.inactive());
    }

    private void givenActivePlan(String planCode) {
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(activePlan(planCode));
    }

    private void givenPlanEntitlements(String planCode, List<String> mandalaIds) {
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(planCode)).thenReturn(mandalaIds);
    }

    private void givenSubscriptionPackEntitlements(String planCode) {
        givenPlanEntitlements(planCode, subscriptionPackIds());
    }

    private void givenUnusedPlanEntitlements(String planCode) {
        // El plan esta expirado (membresia inactiva), por lo que el caso de uso nunca consulta
        // las habilitaciones: el stub es lenient para evitar UnnecessaryStubbingException.
        lenient().when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(planCode))
                .thenReturn(subscriptionPackIds());
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

    private GetCurrentMembershipResponse activePlan(String planCode) {
        return new GetCurrentMembershipResponse(true, planCode, null, Instant.now().plusSeconds(3600));
    }

    // --- act ---

    private List<AvailableMandala> listAvailableMandalas() {
        return useCase.execute(USER_ID);
    }

    private ListAvailableMandalasResponse listAvailableMandalasPage(int page, int size) {
        return useCase.execute(new ListAvailableMandalasRequest(USER_ID, page, size));
    }

    // --- assert ---

    private void thenSizeIs(List<AvailableMandala> result, int expectedSize) {
        assertThat(result).hasSize(expectedSize);
    }

    private void thenUnlockedMandalaIdsAre(List<AvailableMandala> result, String... expectedIds) {
        assertThat(unlocked(result)).extracting(item -> item.getMandala().getId())
                .containsExactly(expectedIds);
    }

    private void thenUnlockSourcesAre(List<AvailableMandala> result, MandalaUnlockSource... expectedSources) {
        assertThat(unlocked(result)).extracting(AvailableMandala::getUnlockSource)
                .containsExactly(expectedSources);
    }

    private void thenUnlockSourceAtUnlockedIndexIs(List<AvailableMandala> result, int index,
            MandalaUnlockSource expectedSource) {
        assertThat(unlocked(result).get(index).getUnlockSource()).isEqualTo(expectedSource);
    }

    private void thenPageContentIdsAre(ListAvailableMandalasResponse result, String... expectedIds) {
        assertThat(result.content()).extracting(MandalaItem::id).containsExactly(expectedIds);
    }

    private void thenPageContentIsEmpty(ListAvailableMandalasResponse result) {
        assertThat(result.content()).isEmpty();
    }

    private void thenPageMetadata(ListAvailableMandalasResponse result, int pageNumber, int pageSize,
            long totalElements, int totalPages, boolean first, boolean last) {
        assertThat(result.pageNumber()).isEqualTo(pageNumber);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        assertThat(result.totalElements()).isEqualTo(totalElements);
        assertThat(result.totalPages()).isEqualTo(totalPages);
        assertThat(result.first()).isEqualTo(first);
        assertThat(result.last()).isEqualTo(last);
    }

    private void thenPageItemIsLocked(ListAvailableMandalasResponse result, int index) {
        MandalaItem item = result.content().get(index);
        assertThat(item.locked()).isTrue();
        assertThat(item.unlockSource()).isNull();
    }

    private List<AvailableMandala> unlocked(List<AvailableMandala> result) {
        return result.stream().filter(mandala -> !mandala.isLocked()).toList();
    }
}
