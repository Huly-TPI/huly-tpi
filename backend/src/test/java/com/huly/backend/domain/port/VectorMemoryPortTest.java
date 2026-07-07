package com.huly.backend.domain.port;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorMemoryPortTest {

    private static final Long USER_ID = 7L;
    private static final String QUERY_TEXT = "necesito ayuda";
    private static final double THRESHOLD = 0.5;

    private final StubVectorMemoryPort port = new StubVectorMemoryPort();

    @Test
    @DisplayName("Lanza IllegalArgumentException cuando la query es null")
    void findRelevantMemoriesShouldThrowWhenQueryIsNull() {
        // --- arrange ---
        SearchVectorMemoriesQuery query = givenNullQuery();

        // --- act & assert ---
        thenSourceTypesAreRequired(query);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException cuando sourceTypes es null")
    void findRelevantMemoriesShouldThrowWhenSourceTypesIsNull() {
        // --- arrange ---
        SearchVectorMemoriesQuery query = givenQueryWithNullSourceTypes();

        // --- act & assert ---
        thenSourceTypesAreRequired(query);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException cuando sourceTypes está vacío")
    void findRelevantMemoriesShouldThrowWhenSourceTypesIsEmpty() {
        // --- arrange ---
        SearchVectorMemoriesQuery query = givenQueryWithEmptySourceTypes();

        // --- act & assert ---
        thenSourceTypesAreRequired(query);
    }

    @Test
    @DisplayName("Devuelve todas las memorias ordenadas por score cuando el límite es null")
    void findRelevantMemoriesShouldReturnAllSortedByScoreWhenLimitIsNull() {
        // --- arrange ---
        givenStoredMemories();
        SearchVectorMemoriesQuery query = givenQueryWithoutLimit();

        // --- act ---
        List<VectorMemory> result = findRelevantMemories(query);

        // --- assert ---
        thenMemoriesAreReturnedInScoreOrder(result, "m2", "m3", "m1");
    }

    @Test
    @DisplayName("Recorta las memorias al límite indicado conservando las de mayor score")
    void findRelevantMemoriesShouldTruncateWhenLimitIsSet() {
        // --- arrange ---
        givenStoredMemories();
        SearchVectorMemoriesQuery query = givenQueryWithLimit(2);

        // --- act ---
        List<VectorMemory> result = findRelevantMemories(query);

        // --- assert ---
        thenMemoriesAreReturnedInScoreOrder(result, "m2", "m3");
    }

    // --- arrange ---

    private SearchVectorMemoriesQuery givenNullQuery() {
        return null;
    }

    private SearchVectorMemoriesQuery givenQueryWithNullSourceTypes() {
        return new SearchVectorMemoriesQuery(USER_ID, null, QUERY_TEXT, null, THRESHOLD);
    }

    private SearchVectorMemoriesQuery givenQueryWithEmptySourceTypes() {
        return new SearchVectorMemoriesQuery(USER_ID, List.of(), QUERY_TEXT, null, THRESHOLD);
    }

    private void givenStoredMemories() {
        port.stub(VectorMemorySource.CHATBOT,
                List.of(memory("m1", VectorMemorySource.CHATBOT, 0.3)));
        port.stub(VectorMemorySource.EMOTIONAL_JOURNAL, List.of(
                memory("m2", VectorMemorySource.EMOTIONAL_JOURNAL, 0.9),
                memory("m3", VectorMemorySource.EMOTIONAL_JOURNAL, 0.6)));
    }

    private SearchVectorMemoriesQuery givenQueryWithoutLimit() {
        return new SearchVectorMemoriesQuery(USER_ID, sourceTypesIncludingNull(), QUERY_TEXT, null, THRESHOLD);
    }

    private SearchVectorMemoriesQuery givenQueryWithLimit(int limit) {
        return new SearchVectorMemoriesQuery(USER_ID, sourceTypesIncludingNull(), QUERY_TEXT, limit, THRESHOLD);
    }

    private List<VectorMemorySource> sourceTypesIncludingNull() {
        return Arrays.asList(VectorMemorySource.CHATBOT, null, VectorMemorySource.EMOTIONAL_JOURNAL);
    }

    private VectorMemory memory(String id, VectorMemorySource sourceType, Double score) {
        return new VectorMemory(id, USER_ID, sourceType, "src-" + id, "contenido " + id, Map.of(), score);
    }

    // --- act ---

    private List<VectorMemory> findRelevantMemories(SearchVectorMemoriesQuery query) {
        return port.findRelevantMemories(query);
    }

    // --- assert ---

    private void thenSourceTypesAreRequired(SearchVectorMemoriesQuery query) {
        assertThatThrownBy(() -> port.findRelevantMemories(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceTypes are required");
    }

    private void thenMemoriesAreReturnedInScoreOrder(List<VectorMemory> result, String... expectedIdsInOrder) {
        assertThat(result)
                .extracting(VectorMemory::id)
                .containsExactly(expectedIdsInOrder);
        assertThat(result)
                .extracting(VectorMemory::score)
                .isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    /**
     * Doble de prueba del puerto: solo la sobrecarga por {@link SearchVectorMemoryQuery} es real y devuelve las
     * memorias configuradas por sourceType; el resto de métodos abstractos no participan en la lógica bajo prueba
     * (la del método default {@code findRelevantMemories(SearchVectorMemoriesQuery)}).
     */
    private static final class StubVectorMemoryPort implements VectorMemoryPort {

        private final Map<VectorMemorySource, List<VectorMemory>> memoriesBySource =
                new EnumMap<>(VectorMemorySource.class);

        void stub(VectorMemorySource sourceType, List<VectorMemory> memories) {
            memoriesBySource.put(sourceType, memories);
        }

        @Override
        public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
            return memoriesBySource.getOrDefault(query.sourceType(), List.of());
        }

        @Override
        public void saveMemory(SaveVectorMemoryCommand command) {
            // Sin comportamiento: no interviene en la lógica del método default bajo prueba.
        }

        @Override
        public List<String> findMemoryContentsByUserIdExcludingSummary(Long userId) {
            // Sin comportamiento: no interviene en la lógica del método default bajo prueba.
            return List.of();
        }

        @Override
        public List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId) {
            // Sin comportamiento: no interviene en la lógica del método default bajo prueba.
            return List.of();
        }

        @Override
        public void deleteMemories(DeleteVectorMemoryCommand command) {
            // Sin comportamiento: no interviene en la lógica del método default bajo prueba.
        }
    }
}
