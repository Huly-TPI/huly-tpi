package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.ListCloudThoughtsRequest;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsResponse;
import com.huly.backend.domain.dto.cloud.CloudThoughtItem;
import com.huly.backend.domain.mapper.cloud.ListCloudThoughtsMapper;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCloudThoughtsUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private CloudThoughtRepository cloudThoughtRepository;

    private ListCloudThoughtsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListCloudThoughtsUseCase(cloudThoughtRepository, new ListCloudThoughtsMapper());
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el usuario no tiene pensamientos")
    void executeShouldReturnEmptyWhenUserHasNoThoughts() {
        givenThoughts();

        ListCloudThoughtsResponse result = list();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea los pensamientos del usuario a items de la respuesta")
    void executeShouldMapThoughtsToItems() {
        givenThoughts(
                thought(1L, "uno", false),
                thought(2L, "dos", true));

        ListCloudThoughtsResponse result = list();

        thenItemsAre(result);
    }

    // --- arrange ---

    private void givenThoughts(CloudThought... thoughts) {
        when(cloudThoughtRepository.findAllByUserId(USER_ID)).thenReturn(List.of(thoughts));
    }

    private CloudThought thought(Long id, String text, boolean workedOn) {
        return CloudThought.builder()
                .id(id)
                .userId(USER_ID)
                .text(text)
                .status(CloudStatus.ACTIVE)
                .workedOn(workedOn)
                .createdAt(CREATED_AT)
                .build();
    }

    // --- act ---

    private ListCloudThoughtsResponse list() {
        return useCase.execute(new ListCloudThoughtsRequest(USER_ID));
    }

    // --- assert ---

    private void thenEmpty(ListCloudThoughtsResponse result) {
        assertThat(result.thoughts()).isEmpty();
    }

    private void thenItemsAre(ListCloudThoughtsResponse result) {
        assertThat(result.thoughts()).extracting(CloudThoughtItem::id).containsExactly(1L, 2L);
        assertThat(result.thoughts()).extracting(CloudThoughtItem::text).containsExactly("uno", "dos");
        assertThat(result.thoughts()).extracting(CloudThoughtItem::workedOn).containsExactly(false, true);
        assertThat(result.thoughts()).extracting(CloudThoughtItem::createdAt).containsOnly(CREATED_AT);
    }
}
