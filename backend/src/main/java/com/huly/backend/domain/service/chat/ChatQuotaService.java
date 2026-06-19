package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.dto.payment.UserPlan;
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

    /**
     * Lanza {@link BusinessRuleException} (→ HTTP 400) si el usuario superó su tope diario.
     * No hace nada si su plan no tiene tope (chat_daily_limit NULL = ilimitado).
     */
    public void assertWithinLimit(Long userId) {
        Instant now = Instant.now();
        Optional<UserPlan> activePlan = userPlanRepository.findByUser(userId)
                .filter(p -> p.isActive(now));

        Integer limit;
        if (activePlan.isPresent()) {
            Long productId = activePlan.get().getProductId();
            limit = productId == null
                    ? null
                    : productRepository.findById(productId).map(Product::getChatDailyLimit).orElse(null);
        } else {
            limit = FREE_DAILY_LIMIT;
        }

        if (limit == null) {
            return; // plan sin tope (ilimitado)
        }

        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        long usedToday = chatMessageRepository.countUserMessagesSince(userId, startOfDay);
        if (usedToday >= limit) {
            throw new BusinessRuleException(activePlan.isPresent()
                    ? "Alcanzaste el límite diario de " + limit + " mensajes de tu plan. Volvé a intentarlo mañana."
                    : "Alcanzaste el límite diario de " + limit
                            + " mensajes del plan gratuito. Suscribite a un plan para seguir usando el chat.");
        }
    }
}
