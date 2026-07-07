package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ChatConfigMapper;
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
class ChatConfigRepositoryImplTest {

    private static final Long CONFIG_ID = 1L;
    private static final Long MISSING_ID = 99L;
    private static final String PROMPT = "prompt";

    @Mock private IChatConfigJpaRepository jpa;
    @Mock private ChatConfigMapper mapper;

    @InjectMocks
    private ChatConfigRepositoryImpl repository;

    @Test
    @DisplayName("Devuelve el dominio mapeado cuando la entidad existe al buscar por id")
    void findByIdShouldReturnMappedDomainWhenEntityExists() {
        ChatConfigEntity entity = entity(CONFIG_ID, PROMPT);
        ChatConfig domain = domain(CONFIG_ID, PROMPT);
        givenFoundById(CONFIG_ID, entity);
        givenMappedToDomain(entity, domain);

        Optional<ChatConfig> result = findById(CONFIG_ID);

        thenResultContains(result, domain);
    }

    @Test
    @DisplayName("Devuelve vacío cuando la entidad no existe al buscar por id")
    void findByIdShouldReturnEmptyWhenEntityNotExists() {
        givenNotFoundById(MISSING_ID);

        Optional<ChatConfig> result = findById(MISSING_ID);

        thenResultEmpty(result);
    }

    @Test
    @DisplayName("Mapea a entidad, guarda y vuelve a mapear a dominio al guardar")
    void saveShouldMapSaveAndMapBack() {
        ChatConfig domain = domain(CONFIG_ID, "p");
        ChatConfigEntity entity = entity(CONFIG_ID, "p");
        ChatConfigEntity saved = entity(CONFIG_ID, "p");
        ChatConfig savedDomain = domain(CONFIG_ID, "p");
        givenMappedToEntity(domain, entity);
        givenSaved(entity, saved);
        givenMappedToDomain(saved, savedDomain);

        ChatConfig result = save(domain);

        thenResultIs(result, savedDomain);
        thenSaved(entity);
    }

    @Test
    @DisplayName("Devuelve el primer dominio mapeado cuando existe alguna entidad")
    void findFirstShouldReturnFirstMappedDomainWhenAnyExists() {
        ChatConfigEntity entity = entity(CONFIG_ID, null);
        ChatConfig domain = domain(CONFIG_ID, null);
        givenAllEntities(entity);
        givenMappedToDomain(entity, domain);

        Optional<ChatConfig> result = findFirst();

        thenResultContains(result, domain);
    }

    @Test
    @DisplayName("Devuelve vacío cuando no existen entidades al buscar el primero")
    void findFirstShouldReturnEmptyWhenNoEntitiesExist() {
        givenAllEntities();

        Optional<ChatConfig> result = findFirst();

        thenResultEmpty(result);
    }

    // --- arrange ---
    private void givenFoundById(Long id, ChatConfigEntity entity) {
        when(jpa.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenNotFoundById(Long id) {
        when(jpa.findById(id)).thenReturn(Optional.empty());
    }

    private void givenMappedToEntity(ChatConfig domain, ChatConfigEntity entity) {
        when(mapper.toEntity(domain)).thenReturn(entity);
    }

    private void givenSaved(ChatConfigEntity entity, ChatConfigEntity saved) {
        when(jpa.save(entity)).thenReturn(saved);
    }

    private void givenMappedToDomain(ChatConfigEntity entity, ChatConfig domain) {
        when(mapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenAllEntities(ChatConfigEntity... entities) {
        when(jpa.findAll()).thenReturn(List.of(entities));
    }

    private ChatConfigEntity entity(Long id, String prompt) {
        return ChatConfigEntity.builder().id(id).systemPrompt(prompt).build();
    }

    private ChatConfig domain(Long id, String prompt) {
        return ChatConfig.builder().id(id).systemPrompt(prompt).build();
    }

    // --- act ---
    private Optional<ChatConfig> findById(Long id) {
        return repository.findById(id);
    }

    private ChatConfig save(ChatConfig domain) {
        return repository.save(domain);
    }

    private Optional<ChatConfig> findFirst() {
        return repository.findFirst();
    }

    // --- assert ---
    private void thenResultContains(Optional<ChatConfig> result, ChatConfig expected) {
        assertThat(result).contains(expected);
    }

    private void thenResultEmpty(Optional<ChatConfig> result) {
        assertThat(result).isEmpty();
    }

    private void thenResultIs(ChatConfig result, ChatConfig expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenSaved(ChatConfigEntity entity) {
        verify(jpa).save(entity);
    }
}
