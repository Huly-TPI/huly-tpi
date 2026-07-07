package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    private static final Long USER_ID = 42L;
    private static final Long PRODUCT_ID = 7L;
    private static final Long OTHER_PRODUCT_ID = 8L;
    private static final Long MISSING_PRODUCT_ID = 99L;
    private static final Long ROW_ID = 99L;
    private static final String PLAN_CODE = "PREMIUM";
    private static final String OTHER_PLAN_CODE = "PRO";
    private static final Instant AS_OF = Instant.parse("2026-06-01T00:00:00Z");

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserPlanRepository userPlanRepository;

    @InjectMocks
    private PlanService planService;

    private Instant activationStart;

    @Test
    @DisplayName("Otorga un mes cuando el usuario no tiene membresía")
    void activateShouldGrantOneMonthWhenUserHasNoMembership() {
        givenProduct(premium());
        givenNoExistingPlan();
        givenSaveReturnsArgument();

        activate(PRODUCT_ID);

        UserPlan saved = captureSaved();
        thenSavedPlanMatches(saved, USER_ID, PRODUCT_ID, PLAN_CODE);
        thenSavedPlanHasNoId(saved);
        thenSavedPlanExpiresAboutOneMonthAfterActivation(saved);
    }

    @Test
    @DisplayName("Acumula sobre el vencimiento actual al renovar el mismo plan vigente")
    void activateShouldStackOntoCurrentExpiryWhenRenewingSameActivePlan() {
        UserPlan active = activeSamePlan();
        givenProduct(premium());
        givenExistingPlan(active);
        givenSaveReturnsArgument();

        activate(PRODUCT_ID);

        UserPlan saved = captureSaved();
        thenSavedPlanId(saved, ROW_ID);
        thenSavedPlanStacksOnto(saved, active);
    }

    @Test
    @DisplayName("Reinicia desde ahora cuando el mismo plan ya está vencido")
    void activateShouldRestartFromNowWhenSamePlanAlreadyExpired() {
        givenProduct(premium());
        givenExistingPlan(expiredSamePlan());
        givenSaveReturnsArgument();

        activate(PRODUCT_ID);

        UserPlan saved = captureSaved();
        thenSavedPlanId(saved, ROW_ID);
        thenSavedPlanExpiresAboutOneMonthAfterActivation(saved);
    }

    @Test
    @DisplayName("Cambia de plan sobrescribiendo la única fila al comprar un plan distinto")
    void activateShouldSwitchPlanOverwritingTheSingleRowWhenBuyingDifferentPlan() {
        givenProduct(premium());
        givenExistingPlan(activeDifferentPlan());
        givenSaveReturnsArgument();

        activate(PRODUCT_ID);

        UserPlan saved = captureSaved();
        thenSavedPlanId(saved, ROW_ID);
        thenSavedPlanMatches(saved, USER_ID, PRODUCT_ID, PLAN_CODE);
        thenSavedPlanExpiresAboutOneMonthAfterActivation(saved);
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el producto no existe")
    void activateShouldThrowResourceNotFoundWhenProductMissing() {
        givenProductNotFound(MISSING_PRODUCT_ID);

        thenActivateThrowsResourceNotFound(MISSING_PRODUCT_ID);
        thenPlanNeverSaved();
    }

    @Test
    @DisplayName("Indica plan activo cuando no está vencido")
    void hasActivePlanShouldBeTrueWhenPlanNotExpired() {
        givenExistingPlan(activeMembership());

        boolean result = hasActivePlan();

        thenHasActivePlan(result);
    }

    @Test
    @DisplayName("Indica plan inactivo cuando está vencido")
    void hasActivePlanShouldBeFalseWhenPlanExpired() {
        givenExistingPlan(expiredMembership());

        boolean result = hasActivePlan();

        thenHasNoActivePlan(result);
    }

    @Test
    @DisplayName("Indica plan inactivo cuando no hay membresía")
    void hasActivePlanShouldBeFalseWhenNoMembership() {
        givenNoExistingPlan();

        boolean result = hasActivePlan();

        thenHasNoActivePlan(result);
    }

    // --- arrange ---
    private void givenProduct(Product product) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    }

    private void givenProductNotFound(Long productId) {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
    }

    private void givenNoExistingPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenExistingPlan(UserPlan plan) {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(plan));
    }

    private void givenSaveReturnsArgument() {
        when(userPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product premium() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Plan Premium")
                .price(new BigDecimal("9999.00"))
                .coinsAmount(0)
                .type(ProductType.PLAN)
                .planCode(PLAN_CODE)
                .build();
    }

    private UserPlan activeSamePlan() {
        return UserPlan.builder()
                .id(ROW_ID).userId(USER_ID).productId(PRODUCT_ID).planCode(PLAN_CODE)
                .grantedAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(10, ChronoUnit.DAYS))
                .build();
    }

    private UserPlan expiredSamePlan() {
        return UserPlan.builder()
                .id(ROW_ID).userId(USER_ID).productId(PRODUCT_ID).planCode(PLAN_CODE)
                .grantedAt(Instant.now().minus(40, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .build();
    }

    private UserPlan activeDifferentPlan() {
        return UserPlan.builder()
                .id(ROW_ID).userId(USER_ID).productId(OTHER_PRODUCT_ID).planCode(OTHER_PLAN_CODE)
                .grantedAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .build();
    }

    private UserPlan activeMembership() {
        return UserPlan.builder().userId(USER_ID).expiresAt(AS_OF.plus(5, ChronoUnit.DAYS)).build();
    }

    private UserPlan expiredMembership() {
        return UserPlan.builder().userId(USER_ID).expiresAt(AS_OF.minus(1, ChronoUnit.DAYS)).build();
    }

    // --- act ---
    private void activate(Long productId) {
        activationStart = Instant.now();
        planService.activate(USER_ID, productId);
    }

    private boolean hasActivePlan() {
        return planService.hasActivePlan(USER_ID, AS_OF);
    }

    // --- assert ---
    private UserPlan captureSaved() {
        ArgumentCaptor<UserPlan> captor = ArgumentCaptor.forClass(UserPlan.class);
        verify(userPlanRepository).save(captor.capture());
        return captor.getValue();
    }

    private void thenSavedPlanMatches(UserPlan saved, Long userId, Long productId, String planCode) {
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getPlanCode()).isEqualTo(planCode);
    }

    private void thenSavedPlanHasNoId(UserPlan saved) {
        assertThat(saved.getId()).isNull();
    }

    private void thenSavedPlanId(UserPlan saved, Long id) {
        assertThat(saved.getId()).isEqualTo(id);
    }

    private void thenSavedPlanExpiresAboutOneMonthAfterActivation(UserPlan saved) {
        assertThat(saved.getExpiresAt()).isAfter(activationStart.plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(activationStart.plus(32, ChronoUnit.DAYS));
    }

    private void thenSavedPlanStacksOnto(UserPlan saved, UserPlan previous) {
        assertThat(saved.getExpiresAt()).isAfter(previous.getExpiresAt().plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(previous.getExpiresAt().plus(32, ChronoUnit.DAYS));
        assertThat(saved.getGrantedAt()).isEqualTo(previous.getGrantedAt());
    }

    private void thenActivateThrowsResourceNotFound(Long productId) {
        assertThatThrownBy(() -> planService.activate(USER_ID, productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenPlanNeverSaved() {
        verify(userPlanRepository, never()).save(any());
    }

    private void thenHasActivePlan(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenHasNoActivePlan(boolean result) {
        assertThat(result).isFalse();
    }
}
