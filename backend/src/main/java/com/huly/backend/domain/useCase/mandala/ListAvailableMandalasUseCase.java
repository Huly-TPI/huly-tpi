package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class ListAvailableMandalasUseCase {

    private final MandalaRepository mandalaRepository;
    private final MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;
    private final UserStoreItemRepository userStoreItemRepository;
    private final GetCurrentMembershipUseCase getCurrentMembershipUseCase;

    public List<AvailableMandala> execute(Long userId) {
        List<Mandala> activeMandalas = mandalaRepository.findAllActiveOrderByDisplayOrder();
        Map<String, MandalaUnlockSource> sources = new HashMap<>();

        for (Mandala mandala : activeMandalas) {
            if (mandala.getAccessType() == MandalaAccessType.FREE) {
                sources.put(mandala.getId(), MandalaUnlockSource.FREE);
            }
        }

        Set<String> purchasedIds = new HashSet<>(userStoreItemRepository.findAssetKeysByUserIdAndCategory(userId, ItemCategory.MANDALA));
        for (String mandalaId : purchasedIds) {
            sources.putIfAbsent(mandalaId, MandalaUnlockSource.PURCHASED);
        }

        getCurrentMembershipUseCase.execute(userId)
                .ifPresent(plan -> mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(plan.getPlanCode())
                        .forEach(mandalaId -> sources.putIfAbsent(mandalaId, MandalaUnlockSource.SUBSCRIPTION)));

        return activeMandalas.stream()
                .filter(mandala -> sources.containsKey(mandala.getId()))
                .map(mandala -> AvailableMandala.builder()
                        .mandala(mandala)
                        .unlockSource(sources.get(mandala.getId()))
                        .build())
                .toList();
    }
}
