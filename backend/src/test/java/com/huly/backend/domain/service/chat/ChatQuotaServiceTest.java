package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatQuotaServiceTest {

    @Mock private UserPlanRepository userPlanRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ChatMessageRepository chatMessageRepository;

    @InjectMocks private ChatQuotaService service;

    private UserPlan activePlan(Long productId) {
        return UserPlan.builder()
                .id(1L).userId(10L).productId(productId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    private Product planWithLimit(Long id, Integer chatDailyLimit) {
        return Product.builder()
                .id(id).name("Plan PREMIUM").description("Suscripción")
                .type(ProductType.PLAN).planCode("PREMIUM").coinsAmount(0)
                .chatDailyLimit(chatDailyLimit)
                .build();
    }

    // ── free (sin plan): tope fijo de 5 ──────────────────────────────────────

    @Test
    void assertWithinLimit_shouldPass_whenFreeUserUnderDailyLimit() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.empty());
        when(chatMessageRepository.countUserMessagesSince(eq(10L), any())).thenReturn(4L);

        assertThatCode(() -> service.assertWithinLimit(10L)).doesNotThrowAnyException();

        verifyNoInteractions(productRepository);
    }

    @Test
    void assertWithinLimit_shouldThrow_whenFreeUserReachedDailyLimit() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.empty());
        when(chatMessageRepository.countUserMessagesSince(eq(10L), any())).thenReturn(5L);

        assertThatThrownBy(() -> service.assertWithinLimit(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("plan gratuito");
    }

    // ── con plan: tope tomado de la DB (chat_daily_limit del producto) ───────

    @Test
    void assertWithinLimit_shouldPass_whenPlanHasNullLimit_withoutCounting() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(activePlan(5L)));
        when(productRepository.findById(5L)).thenReturn(Optional.of(planWithLimit(5L, null)));

        assertThatCode(() -> service.assertWithinLimit(10L)).doesNotThrowAnyException();

        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void assertWithinLimit_shouldPass_whenPlanUserUnderPlanLimit() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(activePlan(5L)));
        when(productRepository.findById(5L)).thenReturn(Optional.of(planWithLimit(5L, 20)));
        when(chatMessageRepository.countUserMessagesSince(eq(10L), any())).thenReturn(19L);

        assertThatCode(() -> service.assertWithinLimit(10L)).doesNotThrowAnyException();
    }

    @Test
    void assertWithinLimit_shouldThrow_whenPlanUserReachedPlanLimit() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(activePlan(5L)));
        when(productRepository.findById(5L)).thenReturn(Optional.of(planWithLimit(5L, 20)));
        when(chatMessageRepository.countUserMessagesSince(eq(10L), any())).thenReturn(20L);

        assertThatThrownBy(() -> service.assertWithinLimit(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("de tu plan");
    }

    @Test
    void assertWithinLimit_shouldTreatExpiredPlanAsFree() {
        UserPlan expired = UserPlan.builder()
                .id(1L).userId(10L).productId(5L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(expired));
        when(chatMessageRepository.countUserMessagesSince(eq(10L), any())).thenReturn(5L);

        assertThatThrownBy(() -> service.assertWithinLimit(10L))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(productRepository);
    }
}
