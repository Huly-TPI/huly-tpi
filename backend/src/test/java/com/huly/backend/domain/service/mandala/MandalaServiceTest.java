package com.huly.backend.domain.service.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.mandala.MandalaPlanEntitlementRepository;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandalaServiceTest {

    private static final Long USER_ID = 7L;
    private static final String MANDALA_ID = "mandala-01";

    @Mock
    private MandalaRepository mandalaRepository;
    @Mock
    private MandalaPlanEntitlementRepository mandalaPlanEntitlementRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;
    @Mock
    private UserPlanRepository userPlanRepository;

    @InjectMocks
    private MandalaService mandalaService;

    @Test
    void isMandalaAvailable_nonExistentMandala_returnsFalse() {
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.empty());
        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isFalse();
    }

    @Test
    void isMandalaAvailable_inactiveMandala_returnsFalse() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(false).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));
        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isFalse();
    }

    @Test
    void isMandalaAvailable_freeMandala_returnsTrue() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.FREE).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));
        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isTrue();
    }

    @Test
    void isMandalaAvailable_purchasableMandala_owned_returnsTrue() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.PURCHASABLE).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of(MANDALA_ID));

        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isTrue();
    }

    @Test
    void isMandalaAvailable_purchasableMandala_notOwned_returnsFalse() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.PURCHASABLE).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));
        when(userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, ItemCategory.MANDALA))
                .thenReturn(List.of("other-mandala"));

        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isFalse();
    }

    @Test
    void isMandalaAvailable_subscriptionMandala_activeEntitled_returnsTrue() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.SUBSCRIPTION).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));

        UserPlan activePlan = UserPlan.builder()
                .planCode("premium")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan));
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("premium")).thenReturn(List.of(MANDALA_ID));

        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isTrue();
    }

    @Test
    void isMandalaAvailable_subscriptionMandala_inactivePlan_returnsFalse() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.SUBSCRIPTION).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));

        UserPlan expiredPlan = UserPlan.builder()
                .planCode("premium")
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(expiredPlan));

        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isFalse();
    }

    @Test
    void isMandalaAvailable_subscriptionMandala_notEntitled_returnsFalse() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.SUBSCRIPTION).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));

        UserPlan activePlan = UserPlan.builder()
                .planCode("premium")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan));
        when(mandalaPlanEntitlementRepository.findMandalaIdsByPlanCode("premium")).thenReturn(List.of("other-mandala"));

        assertThat(mandalaService.isMandalaAvailable(USER_ID, MANDALA_ID)).isFalse();
    }

    @Test
    void validateMandalaAvailability_available_doesNotThrow() {
        Mandala mandala = Mandala.builder().id(MANDALA_ID).active(true).accessType(MandalaAccessType.FREE).build();
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.of(mandala));

        mandalaService.validateMandalaAvailability(USER_ID, MANDALA_ID);
    }

    @Test
    void validateMandalaAvailability_notAvailable_throwsException() {
        when(mandalaRepository.findById(MANDALA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mandalaService.validateMandalaAvailability(USER_ID, MANDALA_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mandala no disponible");
    }
}
