package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.infrastructure.repository.entity.MandalaEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandalaRepositoryImplTest {

    @Mock
    private IMandalaJpaRepository jpaRepository;

    @InjectMocks
    private MandalaRepositoryImpl repository;

    @Test
    @DisplayName("Mapea las mandalas activas ordenadas por displayOrder")
    void findAllActiveOrderByDisplayOrderShouldMapEntities() {
        givenActiveMandalas(mandalaEntity("m1"), mandalaEntity("m2"));

        List<Mandala> result = findAllActive();

        thenMandalaIdsAre(result, "m1", "m2");
        thenMandalaFullyMapped(result.get(0), "m1");
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay mandalas activas")
    void findAllActiveOrderByDisplayOrderShouldReturnEmptyWhenNone() {
        givenActiveMandalas();

        List<Mandala> result = findAllActive();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea las mandalas activas del tipo de acceso indicado")
    void findAllActiveByAccessTypeShouldMapEntities() {
        givenActiveMandalasByAccessType(MandalaAccessType.FREE, mandalaEntity("m1"));

        List<Mandala> result = findAllActiveByAccessType(MandalaAccessType.FREE);

        thenMandalaIdsAre(result, "m1");
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay mandalas del tipo indicado")
    void findAllActiveByAccessTypeShouldReturnEmptyWhenNone() {
        givenActiveMandalasByAccessType(MandalaAccessType.PURCHASABLE);

        List<Mandala> result = findAllActiveByAccessType(MandalaAccessType.PURCHASABLE);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve la mandala mapeada cuando existe el id")
    void findByIdShouldReturnMappedMandalaWhenPresent() {
        givenMandalaById("m1", mandalaEntity("m1"));

        Optional<Mandala> result = findById("m1");

        thenMandalaPresent(result, "m1");
    }

    @Test
    @DisplayName("Devuelve vacío cuando no existe el id de mandala")
    void findByIdShouldReturnEmptyWhenAbsent() {
        givenMandalaById("missing", null);

        Optional<Mandala> result = findById("missing");

        thenAbsent(result);
    }

    // --- arrange ---
    private void givenActiveMandalas(MandalaEntity... entities) {
        when(jpaRepository.findAllByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(entities));
    }

    private void givenActiveMandalasByAccessType(MandalaAccessType accessType, MandalaEntity... entities) {
        when(jpaRepository.findAllByActiveTrueAndAccessTypeOrderByDisplayOrderAsc(accessType))
                .thenReturn(List.of(entities));
    }

    private void givenMandalaById(String id, MandalaEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.ofNullable(entity));
    }

    private MandalaEntity mandalaEntity(String id) {
        return MandalaEntity.builder()
                .id(id)
                .title("Mandala " + id)
                .description("desc " + id)
                .assetKey("asset-" + id)
                .displayOrder(1)
                .active(true)
                .accessType(MandalaAccessType.FREE)
                .priceCoins(50)
                .build();
    }

    // --- act ---
    private List<Mandala> findAllActive() {
        return repository.findAllActiveOrderByDisplayOrder();
    }

    private List<Mandala> findAllActiveByAccessType(MandalaAccessType accessType) {
        return repository.findAllActiveByAccessTypeOrderByDisplayOrder(accessType);
    }

    private Optional<Mandala> findById(String id) {
        return repository.findById(id);
    }

    // --- assert ---
    private void thenMandalaIdsAre(List<Mandala> result, String... ids) {
        assertThat(result).extracting(Mandala::getId).containsExactly(ids);
    }

    private void thenMandalaPresent(Optional<Mandala> result, String id) {
        assertThat(result).isPresent();
        thenMandalaFullyMapped(result.get(), id);
    }

    private void thenMandalaFullyMapped(Mandala mandala, String id) {
        assertThat(mandala.getId()).isEqualTo(id);
        assertThat(mandala.getTitle()).isEqualTo("Mandala " + id);
        assertThat(mandala.getDescription()).isEqualTo("desc " + id);
        assertThat(mandala.getAssetKey()).isEqualTo("asset-" + id);
        assertThat(mandala.getDisplayOrder()).isEqualTo(1);
        assertThat(mandala.isActive()).isTrue();
        assertThat(mandala.getAccessType()).isEqualTo(MandalaAccessType.FREE);
        assertThat(mandala.getPriceCoins()).isEqualTo(50);
    }

    private void thenEmpty(List<Mandala> result) {
        assertThat(result).isEmpty();
    }

    private void thenAbsent(Optional<Mandala> result) {
        assertThat(result).isEmpty();
    }
}
