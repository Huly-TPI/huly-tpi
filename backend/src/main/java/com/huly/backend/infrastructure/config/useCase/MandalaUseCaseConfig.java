package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.mandala.ClearMandalaProgressMapper;
import com.huly.backend.domain.mapper.mandala.GetMandalaProgressMapper;
import com.huly.backend.domain.mapper.mandala.GetMandalaSessionStatusMapper;
import com.huly.backend.domain.mapper.mandala.ListAvailableMandalasMapper;
import com.huly.backend.domain.mapper.mandala.SaveMandalaProgressMapper;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaSessionStatusUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MandalaUseCaseConfig {

    @Bean
    public ListAvailableMandalasMapper listAvailableMandalasMapper() {
        return new ListAvailableMandalasMapper();
    }

    @Bean
    public SaveMandalaProgressMapper saveMandalaProgressMapper() {
        return new SaveMandalaProgressMapper();
    }

    @Bean
    public GetMandalaProgressMapper getMandalaProgressMapper() {
        return new GetMandalaProgressMapper();
    }

    @Bean
    public ClearMandalaProgressMapper clearMandalaProgressMapper() {
        return new ClearMandalaProgressMapper();
    }

    @Bean
    public GetMandalaSessionStatusMapper getMandalaSessionStatusMapper() {
        return new GetMandalaSessionStatusMapper();
    }

    @Bean
    public ListAvailableMandalasUseCase listAvailableMandalasUseCase(
            MandalaRepository mandalaRepository,
            MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository,
            UserStoreItemRepository userStoreItemRepository,
            GetCurrentMembershipUseCase getCurrentMembershipUseCase,
            ListAvailableMandalasMapper listAvailableMandalasMapper) {
        return new ListAvailableMandalasUseCase(
                mandalaRepository,
                mandalaPlanEntitlementRepository,
                userStoreItemRepository,
                getCurrentMembershipUseCase,
                listAvailableMandalasMapper);
    }

    @Bean
    public SaveMandalaProgressUseCase saveMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            MandalaService mandalaService,
            SaveMandalaProgressMapper saveMandalaProgressMapper) {
        return new SaveMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                saveMandalaProgressMapper);
    }

    @Bean
    public GetMandalaProgressUseCase getMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            MandalaService mandalaService,
            GetMandalaProgressMapper getMandalaProgressMapper) {
        return new GetMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                getMandalaProgressMapper);
    }

    @Bean
    public ClearMandalaProgressUseCase clearMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            MandalaService mandalaService,
            ClearMandalaProgressMapper clearMandalaProgressMapper) {
        return new ClearMandalaProgressUseCase(mandalaProgressRepository, mandalaService,
                clearMandalaProgressMapper);
    }

    @Bean
    public GetMandalaSessionStatusUseCase getMandalaSessionStatusUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            MandalaService mandalaService,
            GetMandalaSessionStatusMapper getMandalaSessionStatusMapper) {
        return new GetMandalaSessionStatusUseCase(
                mandalaProgressRepository,
                mandalaService,
                getMandalaSessionStatusMapper);
    }

    @Bean
    public MandalaService mandalaService(
            MandalaRepository mandalaRepository,
            MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository,
            UserStoreItemRepository userStoreItemRepository,
            UserPlanRepository userPlanRepository) {
        return new MandalaService(
                mandalaRepository,
                mandalaPlanEntitlementRepository,
                userStoreItemRepository,
                userPlanRepository);
    }


}
