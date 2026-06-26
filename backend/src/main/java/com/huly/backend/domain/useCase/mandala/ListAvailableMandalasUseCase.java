package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ListAvailableMandalasRequest;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    private final ListAvailableMandalasMapper mapper;

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

        GetCurrentMembershipResponse membership =
                getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(userId));
        if (membership.active()) {
            mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode(membership.planCode())
                    .forEach(mandalaId -> sources.putIfAbsent(mandalaId, MandalaUnlockSource.SUBSCRIPTION));
        }

        return activeMandalas.stream()
                .map(mandala -> AvailableMandala.builder()
                        .mandala(mandala)
                        .unlockSource(sources.get(mandala.getId()))
                        .locked(!sources.containsKey(mandala.getId()))
                        .build())
                .toList();
    }

    public ListAvailableMandalasResponse execute(ListAvailableMandalasRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by("displayOrder").ascending());
        return mapper.toResponse(paginate(request.userId(), pageable));
    }

    private Page<AvailableMandala> paginate(Long userId, Pageable pageable) {
        List<AvailableMandala> availableMandalas = execute(userId);
        long offset = pageable.getOffset();
        if (offset >= availableMandalas.size()) {
            return new PageImpl<>(List.of(), pageable, availableMandalas.size());
        }

        int start = (int) offset;
        int end = Math.min(start + pageable.getPageSize(), availableMandalas.size());
        return new PageImpl<>(availableMandalas.subList(start, end), pageable, availableMandalas.size());
    }
}
