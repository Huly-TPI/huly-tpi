package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VectorMemoryRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private VectorMemoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new VectorMemoryRepositoryImpl(jdbcTemplate);
    }

    @Test
    void findMemoriesByUserIdExcludingSummary_shouldReturnMappedList() {
        VectorMemoryEntry entry = new VectorMemoryEntry("1", "content", "source", "type", "date");

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString()))
                .thenReturn(List.of(entry));

        List<VectorMemoryEntry> result = repository.findMemoriesByUserIdExcludingSummary(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("1");
        assertThat(result.get(0).content()).isEqualTo("content");
    }

    @Test
    void findMemoriesByUserIdExcludingSummary_shouldReturnEmptyList_onException() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString()))
                .thenThrow(new RuntimeException("DB Error"));

        List<VectorMemoryEntry> result = repository.findMemoriesByUserIdExcludingSummary(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void rowMapper_shouldMapCorrectly() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("id")).thenReturn("100");
        when(rs.getString("content")).thenReturn("Hello");
        when(rs.getString("metadata")).thenReturn("{\"sourceType\":\"INPUT\",\"contentType\":\"TEXT_MEMORY\",\"createdAt\":\"2026-06-16T00:00:00Z\"}");

        doAnswer(invocation -> {
            RowMapper<VectorMemoryEntry> mapper = invocation.getArgument(1);
            VectorMemoryEntry mapped = mapper.mapRow(rs, 1);
            assertThat(mapped.id()).isEqualTo("100");
            assertThat(mapped.content()).isEqualTo("Hello");
            assertThat(mapped.sourceType()).isEqualTo("INPUT");
            assertThat(mapped.contentType()).isEqualTo("TEXT_MEMORY");
            assertThat(mapped.createdAt()).isEqualTo("2026-06-16T00:00:00Z");
            return List.of(mapped);
        }).when(jdbcTemplate).query(anyString(), any(RowMapper.class), anyString());

        repository.findMemoriesByUserIdExcludingSummary(1L);
    }

    @Test
    void rowMapper_shouldHandleParsingErrorAndMissingKeys() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("id")).thenReturn("100");
        when(rs.getString("content")).thenReturn("Hello");
        when(rs.getString("metadata")).thenReturn("invalid_json");

        doAnswer(invocation -> {
            RowMapper<VectorMemoryEntry> mapper = invocation.getArgument(1);
            VectorMemoryEntry mapped = mapper.mapRow(rs, 1);
            assertThat(mapped.id()).isEqualTo("100");
            assertThat(mapped.content()).isEqualTo("Hello");
            assertThat(mapped.sourceType()).isEqualTo("UNKNOWN");
            assertThat(mapped.contentType()).isEqualTo("TEXT_MEMORY");
            assertThat(mapped.createdAt()).isNull();
            return List.of(mapped);
        }).when(jdbcTemplate).query(anyString(), any(RowMapper.class), anyString());

        repository.findMemoriesByUserIdExcludingSummary(1L);
    }
}
