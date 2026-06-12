package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.repository.UserPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PlanService {

    /** Duración de cada periodo de vigencia del plan. */
    private static final int PERIOD_MONTHS = 1;

    private final ProductRepository productRepository;
    private final UserPlanRepository userPlanRepository;

    /**
     * Activa o renueva el plan del usuario. Membresía exclusiva: hay una sola fila por
     * usuario. Si renueva el MISMO plan vigente, extiende desde su vencimiento (acumula);
     * si es un plan distinto, está vencido o no tenía, arranca desde ahora y sobrescribe
     * la fila (cambia plan_code), garantizando un único plan activo a la vez.
     */
    public void activate(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("producto", "id", productId));
        String planCode = product.getPlanCode();

        Instant now = Instant.now();
        UserPlan current = userPlanRepository.findByUser(userId).orElse(null);

        boolean renewSame = current != null
                && planCode.equals(current.getPlanCode())
                && current.isActive(now);

        Instant base = renewSame ? current.getExpiresAt() : now;
        Instant newExpiry = base.atZone(ZoneOffset.UTC).plusMonths(PERIOD_MONTHS).toInstant();

        userPlanRepository.save(UserPlan.builder()
                .id(current != null ? current.getId() : null)
                .userId(userId)
                .planCode(planCode)
                .grantedAt(renewSame ? current.getGrantedAt() : now)
                .expiresAt(newExpiry)
                .build());
    }
}
