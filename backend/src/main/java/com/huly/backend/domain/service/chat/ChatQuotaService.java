package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Valida el límite de uso del chat según el plan del usuario.
 * Usuarios sin plan activo (free): tope diario fijo. Con plan activo: el tope diario
 * que tenga cargado ese plan en la DB (chat_daily_limit); NULL = ilimitado.
 */
@Service
@RequiredArgsConstructor
public class ChatQuotaService {

    /** Tope de mensajes diarios para usuarios sin plan activo. */
    private static final int FREE_DAILY_LIMIT = 5;

    private final UserPlanRepository userPlanRepository;
    private final ProductRepository productRepository;
    private final ChatMessageRepository chatMessageRepository;

    /** Cuota restante para el día y, cuando llega a 0, el mensaje de límite a mostrar. */
    public record RemainingQuota(Integer remaining, String limitMessage) {}

    private record QuotaInfo(Integer limit, boolean isPaidPlan) {}

    /**
     * Lanza {@link BusinessRuleException} (→ HTTP 400) si el usuario superó su tope diario.
     * No hace nada si su plan no tiene tope (chat_daily_limit NULL = ilimitado).
     */
    public void assertWithinLimit(Long userId) {
        QuotaInfo quota = loadQuotaInfo(userId);
        if (quota.limit() == null) return;

        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        long usedToday = chatMessageRepository.countUserMessagesSince(userId, startOfDay);
        if (usedToday >= quota.limit()) {
            throw new BusinessRuleException(buildLimitMessage(quota));
        }
    }

    /**
     * Devuelve los mensajes restantes para hoy y el mensaje de límite si llegó a 0.
     * Llamar DESPUÉS de guardar el mensaje (para reflejar el conteo actualizado).
     * Retorna {@code remaining = null} si el plan es ilimitado.
     */
    public RemainingQuota getRemainingQuota(Long userId) {
        QuotaInfo quota = loadQuotaInfo(userId);
        if (quota.limit() == null) return new RemainingQuota(null, null);

        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        long usedToday = chatMessageRepository.countUserMessagesSince(userId, startOfDay);
        int remaining = (int) Math.max(0, quota.limit() - usedToday);
        String limitMessage = remaining == 0 ? buildLimitMessage(quota) : null;
        return new RemainingQuota(remaining, limitMessage);
    }

    private QuotaInfo loadQuotaInfo(Long userId) {
        Instant now = Instant.now();
        Optional<UserPlan> activePlan = userPlanRepository.findByUser(userId)
                .filter(p -> p.isActive(now));
        if (activePlan.isPresent()) {
            Long productId = activePlan.get().getProductId();
            Integer limit = productId == null
                    ? null
                    : productRepository.findById(productId).map(Product::getChatDailyLimit).orElse(null);
            return new QuotaInfo(limit, true);
        }
        return new QuotaInfo(FREE_DAILY_LIMIT, false);
    }

    private String buildLimitMessage(QuotaInfo quota) {
        return quota.isPaidPlan()
                ? "Alcanzaste el límite diario de " + quota.limit() + " mensajes de tu plan. Volvé a intentarlo mañana."
                : "Alcanzaste el límite diario de " + quota.limit()
                        + " mensajes del plan gratuito. Suscribite a un plan para seguir usando el chat.";
    }
}
