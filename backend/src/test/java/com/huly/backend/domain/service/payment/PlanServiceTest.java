package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private UserPlanRepository userPlanRepository;
    @InjectMocks private PlanService planService;

    private final Product premium = Product.builder()
            .id(7L)
            .name("Plan Premium")
            .price(new BigDecimal("9999.00"))
            .coinsAmount(0)
            .type(ProductType.PLAN)
            .planCode("PREMIUM")
            .build();

    @Test
    void activate_shouldGrantOneMonth_whenUserHasNoMembership() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(42L)).thenReturn(Optional.empty());
        when(userPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        planService.activate(42L, 7L);

        UserPlan saved = captureSaved();
        assertThat(saved.getUserId()).isEqualTo(42L);
        assertThat(saved.getProductId()).isEqualTo(7L);
        assertThat(saved.getPlanCode()).isEqualTo("PREMIUM");
        assertThat(saved.getId()).isNull();
        assertThat(saved.getExpiresAt()).isAfter(before.plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(before.plus(32, ChronoUnit.DAYS));
    }

    @Test
    void activate_shouldStackOntoCurrentExpiry_whenRenewingSameActivePlan() {
        Instant futureExpiry = Instant.now().plus(10, ChronoUnit.DAYS);
        UserPlan active = UserPlan.builder()
                .id(99L).userId(42L).productId(7L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .expiresAt(futureExpiry)
                .build();
        when(productRepository.findById(7L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(42L)).thenReturn(Optional.of(active));
        when(userPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        planService.activate(42L, 7L);

        UserPlan saved = captureSaved();
        assertThat(saved.getId()).isEqualTo(99L);
        // extiende DESDE el vencimiento actual (acumula)
        assertThat(saved.getExpiresAt()).isAfter(futureExpiry.plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(futureExpiry.plus(32, ChronoUnit.DAYS));
        assertThat(saved.getGrantedAt()).isEqualTo(active.getGrantedAt());
    }

    @Test
    void activate_shouldRestartFromNow_whenSamePlanAlreadyExpired() {
        Instant pastExpiry = Instant.now().minus(5, ChronoUnit.DAYS);
        UserPlan expired = UserPlan.builder()
                .id(99L).userId(42L).productId(7L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(40, ChronoUnit.DAYS))
                .expiresAt(pastExpiry)
                .build();
        when(productRepository.findById(7L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(42L)).thenReturn(Optional.of(expired));
        when(userPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        planService.activate(42L, 7L);

        UserPlan saved = captureSaved();
        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(saved.getExpiresAt()).isAfter(before.plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(before.plus(32, ChronoUnit.DAYS));
    }

    @Test
    void activate_shouldSwitchPlan_overwritingTheSingleRow_whenBuyingDifferentPlan() {
        // Usuario con PRO vigente compra PREMIUM → membresía exclusiva: se sobrescribe la fila.
        Instant proExpiry = Instant.now().plus(15, ChronoUnit.DAYS);
        UserPlan activePro = UserPlan.builder()
                .id(99L).userId(42L).productId(8L).planCode("PRO")
                .grantedAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .expiresAt(proExpiry)
                .build();
        when(productRepository.findById(7L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(42L)).thenReturn(Optional.of(activePro));
        when(userPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        planService.activate(42L, 7L);

        UserPlan saved = captureSaved();
        assertThat(saved.getId()).isEqualTo(99L);            // reutiliza la misma fila
        assertThat(saved.getProductId()).isEqualTo(7L);       // ahora apunta al nuevo producto
        assertThat(saved.getPlanCode()).isEqualTo("PREMIUM"); // sobrescribe el plan anterior
        // al cambiar de plan arranca desde ahora, NO acumula el tiempo de PRO
        assertThat(saved.getExpiresAt()).isAfter(before.plus(27, ChronoUnit.DAYS));
        assertThat(saved.getExpiresAt()).isBefore(before.plus(32, ChronoUnit.DAYS));
    }

    @Test
    void activate_shouldThrowResourceNotFound_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.activate(42L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userPlanRepository, never()).save(any());
    }

    private UserPlan captureSaved() {
        ArgumentCaptor<UserPlan> captor = ArgumentCaptor.forClass(UserPlan.class);
        verify(userPlanRepository).save(captor.capture());
        return captor.getValue();
    }
}
