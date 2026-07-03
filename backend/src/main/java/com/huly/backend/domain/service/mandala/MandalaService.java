package com.huly.backend.domain.service.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static java.time.Instant.now;

@RequiredArgsConstructor
public class MandalaService {

    private final MandalaRepository mandalaRepository;
    private final MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;
    private final UserStoreItemRepository userStoreItemRepository;
    private final UserPlanRepository userPlanRepository;

    public void validateMandalaAvailability(Long userId, String mandalaId) {
        if (!isMandalaAvailable(userId, mandalaId)) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }

    public boolean isMandalaAvailable(Long userId, String mandalaId) {
        return mandalaRepository.findById(mandalaId)
                .filter(Mandala::isActive)
                .map(mandala -> {
                    if (mandala.getAccessType() == MandalaAccessType.FREE) {
                        return true;
                    }

                    if (mandala.getAccessType() == MandalaAccessType.PURCHASABLE) {
                        List<String> purchasedIds = userStoreItemRepository.findAssetKeysByUserIdAndCategory(userId, ItemCategory.MANDALA);
                        return purchasedIds.contains(mandalaId);
                    }

                    if (mandala.getAccessType() == MandalaAccessType.SUBSCRIPTION) {
                        return userPlanRepository.findByUser(userId)
                                .filter(plan -> plan.isActive(now()))
                                .map(plan -> {
                                    List<String> entitledMandalas = mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(plan.getPlanCode());
                                    return entitledMandalas.contains(mandalaId);
                                })
                                .orElse(false);
                    }
                    return false;
                })
                .orElse(false);
    }
}
