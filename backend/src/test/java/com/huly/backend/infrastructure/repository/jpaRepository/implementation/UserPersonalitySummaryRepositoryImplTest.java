package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserPersonalitySummary;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserPersonalitySummaryEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPersonalitySummaryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersonalitySummaryRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final Long DELETE_USER_ID = 3L;

    @Mock
    private IUserPersonalitySummaryJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private UserPersonalitySummaryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new UserPersonalitySummaryRepositoryImpl(jpaRepository, appUserRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("Devuelve el resumen dedicado cuando existe en la tabla propia")
    void findByUserIdShouldReturnDedicatedSummaryWhenPresent() {
        givenDedicatedSummary(dedicatedEntity());

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "Perfil");
        thenAcceptedIs(result, "Respiracion");
    }

    @Test
    @DisplayName("Cae al almacén vectorial legado con JSON completo cuando no hay resumen dedicado")
    void findByUserIdShouldFallbackToLegacyVectorStore() {
        givenNoDedicatedSummary();
        givenLegacyContentIsFullJson();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "Resumen legado");
        thenAcceptedIs(result, "Yoga");
        thenRejectedIs(result, "Eventos sociales");
    }

    @Test
    @DisplayName("Devuelve vacío cuando el almacén legado no tiene contenido")
    void findByUserIdShouldReturnEmptyWhenLegacyContentIsEmpty() {
        givenNoDedicatedSummary();
        givenLegacyContentIsEmpty();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenAbsent(result);
    }

    @Test
    @DisplayName("Parsea el JSON legado envuelto en un bloque de código")
    void findByUserIdShouldParseLegacyCodeFencedJson() {
        givenNoDedicatedSummary();
        givenLegacyContentIsCodeFencedJson();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "S");
        thenAcceptedIs(result, "A");
        thenRejectedIs(result, "R");
    }

    @Test
    @DisplayName("Usa el texto plano legado como resumen sin aceptados ni rechazados")
    void findByUserIdShouldUseLegacyPlainTextAsSummary() {
        givenNoDedicatedSummary();
        givenLegacyContentIsPlainText();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "solo texto");
        thenAcceptedIs(result, null);
        thenRejectedIs(result, null);
    }

    @Test
    @DisplayName("Usa el contenido crudo como resumen cuando el JSON legado es inválido")
    void findByUserIdShouldFallBackToRawContentWhenLegacyJsonIsInvalid() {
        givenNoDedicatedSummary();
        givenLegacyContentIsInvalidJson();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "{no es json");
        thenAcceptedIs(result, null);
        thenRejectedIs(result, null);
    }

    @Test
    @DisplayName("Deja aceptados y rechazados nulos cuando el JSON legado no los incluye")
    void findByUserIdShouldLeaveOptionalFieldsNullWhenLegacyJsonOmitsThem() {
        givenNoDedicatedSummary();
        givenLegacyContentIsJsonWithoutOptionalFields();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "S");
        thenAcceptedIs(result, null);
        thenRejectedIs(result, null);
    }

    @Test
    @DisplayName("Devuelve vacío cuando la consulta al almacén legado falla")
    void findByUserIdShouldReturnEmptyWhenLegacyQueryFails() {
        givenNoDedicatedSummary();
        givenLegacyQueryFails();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenAbsent(result);
    }

    @Test
    @DisplayName("Usa el JSON crudo como resumen cuando el JSON legado no trae summary")
    void findByUserIdShouldUseRawJsonWhenLegacyJsonHasNoSummary() {
        givenNoDedicatedSummary();
        givenLegacyContentIsJsonWithoutSummary();

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "{\"accepted\":\"A\",\"rejected\":\"R\"}");
        thenAcceptedIs(result, "A");
        thenRejectedIs(result, "R");
    }

    @Test
    @DisplayName("Mapea el contenido legado a través del RowMapper de la consulta")
    void findByUserIdShouldMapLegacyRowThroughRowMapper() {
        givenNoDedicatedSummary();
        givenLegacyRowMapperReturns("Resumen legado directo");

        Optional<UserPersonalitySummary> result = findByUserId();

        thenPresentSummary(result, "Resumen legado directo");
    }

    @Test
    @DisplayName("Actualiza la fila existente del usuario al guardar")
    void saveShouldUpdateExistingRowForUser() {
        givenExistingSummary(existingEntity());

        UserPersonalitySummary saved = save(updatedSummary());

        thenSavedId(saved, 5L);
        thenSavedSummary(saved, "Nuevo");
    }

    @Test
    @DisplayName("Inserta un nuevo resumen cuando el usuario no tiene uno")
    void saveShouldInsertWhenUserDoesNotHaveSummary() {
        givenNoSummaryAndReferencedUser(appUser());

        UserPersonalitySummary saved = save(insertedSummary());

        thenSavedId(saved, 11L);
        thenSavedUserId(saved, USER_ID);
    }

    @Test
    @DisplayName("Delega el borrado por usuario en el repositorio JPA")
    void deleteByUserIdShouldDelegateToJpaRepository() {
        deleteByUserId(DELETE_USER_ID);

        thenDeletedByUserId(DELETE_USER_ID);
    }

    // --- arrange ---
    private void givenDedicatedSummary(UserPersonalitySummaryEntity entity) {
        when(jpaRepository.findByAppUserId(USER_ID)).thenReturn(Optional.of(entity));
    }

    private void givenNoDedicatedSummary() {
        when(jpaRepository.findByAppUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenLegacyContentIsEmpty() {
        whenLegacyQueryReturns(List.of());
    }

    private void givenLegacyContentIsFullJson() {
        whenLegacyQueryReturns(List.of("""
                {
                  "summary": "Resumen legado",
                  "accepted": "Yoga",
                  "rejected": "Eventos sociales"
                }
                """));
    }

    private void givenLegacyContentIsCodeFencedJson() {
        whenLegacyQueryReturns(List.of("```json\n{\"summary\":\"S\",\"accepted\":\"A\",\"rejected\":\"R\"}\n```"));
    }

    private void givenLegacyContentIsPlainText() {
        whenLegacyQueryReturns(List.of("solo texto"));
    }

    private void givenLegacyContentIsInvalidJson() {
        whenLegacyQueryReturns(List.of("{no es json"));
    }

    private void givenLegacyContentIsJsonWithoutOptionalFields() {
        whenLegacyQueryReturns(List.of("{\"summary\":\"S\"}"));
    }

    private void givenLegacyContentIsJsonWithoutSummary() {
        whenLegacyQueryReturns(List.of("{\"accepted\":\"A\",\"rejected\":\"R\"}"));
    }

    private void whenLegacyQueryReturns(List<String> contents) {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString())).thenReturn(contents);
    }

    private void givenLegacyRowMapperReturns(String content) {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString())).thenAnswer(invocation -> {
            RowMapper<String> rowMapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getString("content")).thenReturn(content);
            return List.of(rowMapper.mapRow(resultSet, 0));
        });
    }

    private void givenLegacyQueryFails() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString()))
                .thenThrow(new RuntimeException("db"));
    }

    private void givenExistingSummary(UserPersonalitySummaryEntity existing) {
        when(jpaRepository.findByAppUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(UserPersonalitySummaryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenNoSummaryAndReferencedUser(AppUserEntity appUser) {
        when(jpaRepository.findByAppUserId(USER_ID)).thenReturn(Optional.empty());
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(appUser);
        when(jpaRepository.save(any(UserPersonalitySummaryEntity.class))).thenAnswer(invocation -> {
            UserPersonalitySummaryEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });
    }

    private UserPersonalitySummaryEntity dedicatedEntity() {
        return UserPersonalitySummaryEntity.builder()
                .id(10L)
                .appUser(AppUserEntity.builder().id(USER_ID).build())
                .summary("Perfil")
                .accepted("Respiracion")
                .rejected("Social")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UserPersonalitySummaryEntity existingEntity() {
        return UserPersonalitySummaryEntity.builder()
                .id(5L)
                .appUser(AppUserEntity.builder().id(USER_ID).build())
                .summary("Viejo")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private AppUserEntity appUser() {
        return AppUserEntity.builder().id(USER_ID).build();
    }

    private UserPersonalitySummary updatedSummary() {
        return UserPersonalitySummary.builder()
                .userId(USER_ID)
                .summary("Nuevo")
                .accepted("Respiracion")
                .rejected("Social")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UserPersonalitySummary insertedSummary() {
        return UserPersonalitySummary.builder()
                .userId(USER_ID)
                .summary("Nuevo")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- act ---
    private Optional<UserPersonalitySummary> findByUserId() {
        return repository.findByUserId(USER_ID);
    }

    private UserPersonalitySummary save(UserPersonalitySummary summary) {
        return repository.save(summary);
    }

    private void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    // --- assert ---
    private void thenPresentSummary(Optional<UserPersonalitySummary> result, String expectedSummary) {
        assertThat(result).isPresent();
        assertThat(result.get().getSummary()).isEqualTo(expectedSummary);
    }

    private void thenAcceptedIs(Optional<UserPersonalitySummary> result, String expected) {
        assertThat(result).isPresent();
        assertThat(result.get().getAccepted()).isEqualTo(expected);
    }

    private void thenRejectedIs(Optional<UserPersonalitySummary> result, String expected) {
        assertThat(result).isPresent();
        assertThat(result.get().getRejected()).isEqualTo(expected);
    }

    private void thenAbsent(Optional<UserPersonalitySummary> result) {
        assertThat(result).isEmpty();
    }

    private void thenSavedId(UserPersonalitySummary saved, Long expectedId) {
        assertThat(saved.getId()).isEqualTo(expectedId);
    }

    private void thenSavedSummary(UserPersonalitySummary saved, String expectedSummary) {
        assertThat(saved.getSummary()).isEqualTo(expectedSummary);
    }

    private void thenSavedUserId(UserPersonalitySummary saved, Long expectedUserId) {
        assertThat(saved.getUserId()).isEqualTo(expectedUserId);
    }

    private void thenDeletedByUserId(Long userId) {
        verify(jpaRepository).deleteByAppUserId(userId);
    }
}
