package com.huly.backend.domain.service.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandalaServiceTest {

    private static final Long USER_ID = 7L;
    private static final String MANDALA_ID = "mandala-01";
    private static final String OTHER_MANDALA_ID = "other-mandala";
    private static final String PLAN_CODE = "premium";

    @Mock
    private MandalaRepository mandalaRepository;
    @Mock
    private MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;
    @Mock
    private UserPlanRepository userPlanRepository;

    @InjectMocks
    private MandalaService mandalaService;

    @Test
    @DisplayName("Devuelve false cuando la mandala no existe")
    void isMandalaAvailableNonExistentMandalaReturnsFalse() {
        givenMandalaNotFound();

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("Devuelve false cuando la mandala está inactiva")
    void isMandalaAvailableInactiveMandalaReturnsFalse() {
        givenMandala(inactiveMandala());

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("Devuelve true cuando la mandala es gratuita")
    void isMandalaAvailableFreeMandalaReturnsTrue() {
        givenMandala(mandalaWithAccess(MandalaAccessType.FREE));

        boolean result = isMandalaAvailable();

        thenAvailable(result);
    }

    @Test
    @DisplayName("Devuelve true cuando la mandala comprable es de su propiedad")
    void isMandalaAvailablePurchasableOwnedReturnsTrue() {
        givenMandala(mandalaWithAccess(MandalaAccessType.PURCHASABLE));
        givenPurchasedMandalas(MANDALA_ID);

        boolean result = isMandalaAvailable();

        thenAvailable(result);
    }

    @Test
    @DisplayName("Devuelve false cuando la mandala comprable no es de su propiedad")
    void isMandalaAvailablePurchasableNotOwnedReturnsFalse() {
        givenMandala(mandalaWithAccess(MandalaAccessType.PURCHASABLE));
        givenPurchasedMandalas(OTHER_MANDALA_ID);

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("Devuelve true cuando la mandala por suscripción está habilitada con plan activo")
    void isMandalaAvailableSubscriptionActiveEntitledReturnsTrue() {
        givenMandala(mandalaWithAccess(MandalaAccessType.SUBSCRIPTION));
        givenUserPlan(activePlan());
        givenEntitledMandalas(MANDALA_ID);

        boolean result = isMandalaAvailable();

        thenAvailable(result);
    }

    @Test
    @DisplayName("Devuelve false cuando la mandala por suscripción tiene el plan vencido")
    void isMandalaAvailableSubscriptionInactivePlanReturnsFalse() {
        givenMandala(mandalaWithAccess(MandalaAccessType.SUBSCRIPTION));
        givenUserPlan(expiredPlan());

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("Devuelve false cuando la mandala por suscripción no está habilitada en el plan")
    void isMandalaAvailableSubscriptionNotEntitledReturnsFalse() {
        givenMandala(mandalaWithAccess(MandalaAccessType.SUBSCRIPTION));
        givenUserPlan(activePlan());
        givenEntitledMandalas(OTHER_MANDALA_ID);

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("Devuelve false cuando la mandala activa no tiene tipo de acceso")
    void isMandalaAvailableUnknownAccessTypeReturnsFalse() {
        givenMandala(activeMandalaWithoutAccessType());

        boolean result = isMandalaAvailable();

        thenNotAvailable(result);
    }

    @Test
    @DisplayName("No lanza excepción cuando la mandala está disponible")
    void validateMandalaAvailabilityAvailableDoesNotThrow() {
        givenMandala(mandalaWithAccess(MandalaAccessType.FREE));

        thenValidationDoesNotThrow();
    }

    @Test
    @DisplayName("Lanza excepción cuando la mandala no está disponible")
    void validateMandalaAvailabilityNotAvailableThrowsException() {
        givenMandalaNotFound();

        thenValidationThrowsResourceNotFound();
    }

    // --- arrange ---
    private void givenMandalaNotFound() {
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.empty());
    }

    private void givenMandala(Mandala mandala) {
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));
    }

    private void givenPurchasedMandalas(String... ids) {
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of(ids));
    }

    private void givenUserPlan(UserPlan plan) {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(plan));
    }

    private void givenEntitledMandalas(String... ids) {
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(PLAN_CODE)).thenReturn(List.of(ids));
    }

    private Mandala inactiveMandala() {
        return Mandala.builder().id(MANDALA_ID).active(false).build();
    }

    private Mandala mandalaWithAccess(MandalaAccessType accessType) {
        return Mandala.builder().id(MANDALA_ID).active(true).accessType(accessType).build();
    }

    private Mandala activeMandalaWithoutAccessType() {
        return Mandala.builder().id(MANDALA_ID).active(true).build();
    }

    private UserPlan activePlan() {
        return UserPlan.builder()
                .planCode(PLAN_CODE)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private UserPlan expiredPlan() {
        return UserPlan.builder()
                .planCode(PLAN_CODE)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
    }

    // --- act ---
    private boolean isMandalaAvailable() {
        return mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID);
    }

    private void validateMandalaAvailability() {
        mandalaService.validateMandalaAvailability(USER_ID, MANDALA_ID);
    }

    // --- assert ---
    private void thenAvailable(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenNotAvailable(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenValidationDoesNotThrow() {
        assertThatCode(this::validateMandalaAvailability).doesNotThrowAnyException();
    }

    private void thenValidationThrowsResourceNotFound() {
        assertThatThrownBy(this::validateMandalaAvailability)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }
}
