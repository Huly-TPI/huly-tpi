package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IAntiScrollConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.AntiScrollGlobalConfigMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiScrollGlobalConfigRepositoryImplTest {

    private static final Long CONFIG_ID = 1L;
    private static final Integer PAUSE_INTERVAL = 15;

    @Mock private IAntiScrollConfigJpaRepository jpa;
    @Mock private AntiScrollGlobalConfigMapper mapper;

    @InjectMocks
    private AntiScrollGlobalConfigRepositoryImpl repository;

    @Test
    @DisplayName("Mapea a entidad, guarda y vuelve a mapear a dominio al guardar")
    void saveShouldMapSaveAndMapBack() {
        AntiScrollGlobalConfig domain = domain(CONFIG_ID, PAUSE_INTERVAL);
        AntiScrollConfigEntity entity = entity(CONFIG_ID, PAUSE_INTERVAL);
        AntiScrollConfigEntity saved = entity(CONFIG_ID, PAUSE_INTERVAL);
        AntiScrollGlobalConfig savedDomain = domain(CONFIG_ID, PAUSE_INTERVAL);
        givenMappedToEntity(domain, entity);
        givenSaved(entity, saved);
        givenMappedToDomain(saved, savedDomain);

        AntiScrollGlobalConfig result = save(domain);

        thenResultIs(result, savedDomain);
        thenSaved(entity);
    }

    @Test
    @DisplayName("Devuelve el primer dominio mapeado cuando existe alguna entidad")
    void findFirstShouldReturnFirstMappedDomainWhenAnyExists() {
        AntiScrollConfigEntity entity = entity(CONFIG_ID, null);
        AntiScrollGlobalConfig domain = domain(CONFIG_ID, null);
        givenAllEntities(entity);
        givenMappedToDomain(entity, domain);

        Optional<AntiScrollGlobalConfig> result = findFirst();

        thenResultContains(result, domain);
    }

    @Test
    @DisplayName("Devuelve vacío cuando no existen entidades al buscar el primero")
    void findFirstShouldReturnEmptyWhenNoEntitiesExist() {
        givenAllEntities();

        Optional<AntiScrollGlobalConfig> result = findFirst();

        thenResultEmpty(result);
    }

    // --- arrange ---
    private void givenMappedToEntity(AntiScrollGlobalConfig domain, AntiScrollConfigEntity entity) {
        when(mapper.toEntity(domain)).thenReturn(entity);
    }

    private void givenSaved(AntiScrollConfigEntity entity, AntiScrollConfigEntity saved) {
        when(jpa.save(entity)).thenReturn(saved);
    }

    private void givenMappedToDomain(AntiScrollConfigEntity entity, AntiScrollGlobalConfig domain) {
        when(mapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenAllEntities(AntiScrollConfigEntity... entities) {
        when(jpa.findAll()).thenReturn(List.of(entities));
    }

    private AntiScrollGlobalConfig domain(Long id, Integer pauseInterval) {
        return AntiScrollGlobalConfig.builder().id(id).defaultPauseIntervalMinutes(pauseInterval).build();
    }

    private AntiScrollConfigEntity entity(Long id, Integer pauseInterval) {
        return AntiScrollConfigEntity.builder().id(id).defaultPauseIntervalMinutes(pauseInterval).build();
    }

    // --- act ---
    private AntiScrollGlobalConfig save(AntiScrollGlobalConfig domain) {
        return repository.save(domain);
    }

    private Optional<AntiScrollGlobalConfig> findFirst() {
        return repository.findFirst();
    }

    // --- assert ---
    private void thenResultIs(AntiScrollGlobalConfig result, AntiScrollGlobalConfig expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenSaved(AntiScrollConfigEntity entity) {
        verify(jpa).save(entity);
    }

    private void thenResultContains(Optional<AntiScrollGlobalConfig> result, AntiScrollGlobalConfig expected) {
        assertThat(result).contains(expected);
    }

    private void thenResultEmpty(Optional<AntiScrollGlobalConfig> result) {
        assertThat(result).isEmpty();
    }
}
