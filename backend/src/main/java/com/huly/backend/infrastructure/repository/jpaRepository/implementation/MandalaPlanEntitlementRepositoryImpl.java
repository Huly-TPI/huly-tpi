package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaPlanEntitlementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MandalaPlanEntitlementRepositoryImpl implements MandalaPlanEntitlementRepository {

    private final IMandalaPlanEntitlementJpaRepository jpaRepository;

    @Override
    public List<String> findMandalaIdsByPlanCode(String planCode) {
        return jpaRepository.findAllByPlanCode(planCode).stream()
                .map(entity -> entity.getMandalaId())
                .toList();
    }
}
