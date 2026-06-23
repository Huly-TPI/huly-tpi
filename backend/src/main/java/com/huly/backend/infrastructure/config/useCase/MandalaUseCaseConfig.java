package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MandalaUseCaseConfig {

    @Bean
    public ListAvailableMandalasUseCase listAvailableMandalasUseCase(
            MandalaRepository mandalaRepository,
            MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository,
            UserStoreItemRepository userStoreItemRepository,
            GetCurrentMembershipUseCase getCurrentMembershipUseCase) {
        return new ListAvailableMandalasUseCase(
                mandalaRepository,
                mandalaPlanEntitlementRepository,
                userStoreItemRepository,
                getCurrentMembershipUseCase);
    }

    @Bean
    public SaveMandalaProgressUseCase saveMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            ListAvailableMandalasUseCase listAvailableMandalasUseCase) {
        return new SaveMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);
    }

    @Bean
    public GetMandalaProgressUseCase getMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            ListAvailableMandalasUseCase listAvailableMandalasUseCase) {
        return new GetMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);
    }

    @Bean
    public ClearMandalaProgressUseCase clearMandalaProgressUseCase(
            MandalaProgressRepository mandalaProgressRepository,
            ListAvailableMandalasUseCase listAvailableMandalasUseCase) {
        return new ClearMandalaProgressUseCase(mandalaProgressRepository, listAvailableMandalasUseCase);
    }
}
