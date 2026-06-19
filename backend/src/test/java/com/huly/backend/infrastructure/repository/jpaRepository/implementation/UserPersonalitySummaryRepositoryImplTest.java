package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.UserPersonalitySummary;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserPersonalitySummaryEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPersonalitySummaryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersonalitySummaryRepositoryImplTest {

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
    void findByUserId_shouldReturnDedicatedSummaryWhenPresent() {
        UserPersonalitySummaryEntity entity = UserPersonalitySummaryEntity.builder()
                .id(10L)
                .appUser(AppUserEntity.builder().id(1L).build())
                .summary("Perfil")
                .accepted("Respiracion")
                .rejected("Social")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(jpaRepository.findByAppUserId(1L)).thenReturn(Optional.of(entity));

        Optional<UserPersonalitySummary> result = repository.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSummary()).isEqualTo("Perfil");
        assertThat(result.get().getAccepted()).isEqualTo("Respiracion");
    }

    @Test
    void findByUserId_shouldFallbackToLegacyVectorStore() {
        when(jpaRepository.findByAppUserId(1L)).thenReturn(Optional.empty());
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of("""
                        {
                          "summary": "Resumen legado",
                          "accepted": "Yoga",
                          "rejected": "Eventos sociales"
                        }
                        """));

        Optional<UserPersonalitySummary> result = repository.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSummary()).isEqualTo("Resumen legado");
        assertThat(result.get().getAccepted()).isEqualTo("Yoga");
        assertThat(result.get().getRejected()).isEqualTo("Eventos sociales");
    }

    @Test
    void save_shouldUpdateExistingRowForUser() {
        UserPersonalitySummaryEntity existing = UserPersonalitySummaryEntity.builder()
                .id(5L)
                .appUser(AppUserEntity.builder().id(1L).build())
                .summary("Viejo")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(jpaRepository.findByAppUserId(1L)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(UserPersonalitySummaryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPersonalitySummary saved = repository.save(UserPersonalitySummary.builder()
                .userId(1L)
                .summary("Nuevo")
                .accepted("Respiracion")
                .rejected("Social")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        assertThat(saved.getId()).isEqualTo(5L);
        assertThat(saved.getSummary()).isEqualTo("Nuevo");
    }

    @Test
    void save_shouldInsertWhenUserDoesNotHaveSummary() {
        AppUserEntity appUser = AppUserEntity.builder().id(1L).build();
        when(jpaRepository.findByAppUserId(1L)).thenReturn(Optional.empty());
        when(appUserRepository.getReferenceById(1L)).thenReturn(appUser);
        when(jpaRepository.save(any(UserPersonalitySummaryEntity.class))).thenAnswer(invocation -> {
            UserPersonalitySummaryEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        UserPersonalitySummary saved = repository.save(UserPersonalitySummary.builder()
                .userId(1L)
                .summary("Nuevo")
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        assertThat(saved.getId()).isEqualTo(11L);
        assertThat(saved.getUserId()).isEqualTo(1L);
    }

    @Test
    void deleteByUserId_shouldDelegateToJpaRepository() {
        repository.deleteByUserId(3L);

        verify(jpaRepository).deleteByAppUserId(3L);
    }
}
