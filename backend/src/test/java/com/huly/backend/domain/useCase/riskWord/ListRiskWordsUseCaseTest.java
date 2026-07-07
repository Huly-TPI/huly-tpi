package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.dto.riskWord.RiskWordItem;
import com.huly.backend.domain.mapper.riskWord.ListRiskWordsMapper;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRiskWordsUseCaseTest {

    private static final String WORD_FILTER = "suicidio";
    private static final Boolean ACTIVE_FILTER = true;
    private static final String SEVERITY_FILTER = "HIGH";
    private static final int PAGE = 0;
    private static final int SIZE = 20;

    private static final Long ITEM_ID = 5L;
    private static final String ITEM_WORD = "suicidio";
    private static final String ITEM_DESCRIPTION = "riesgo alto";
    private static final RiskSeverity ITEM_SEVERITY = RiskSeverity.HIGH;

    @Mock
    private RiskWordService riskWordService;

    private ListRiskWordsUseCase listRiskWordsUseCase;

    @BeforeEach
    void setUp() {
        listRiskWordsUseCase = new ListRiskWordsUseCase(riskWordService, new ListRiskWordsMapper());
    }

    @Test
    @DisplayName("Delega los filtros y la paginación al servicio")
    void executeShouldDelegateFiltersAndPaginationToService() {
        givenServiceReturnsEmptyPage();

        ListRiskWordsResponse result = list();

        thenServiceQueriedWithFiltersAndPageable(result);
    }

    @Test
    @DisplayName("Mapea el contenido y los metadatos de la página a la respuesta")
    void executeShouldMapPageContentAndMetadataToResponse() {
        givenServiceReturnsPopulatedPage();

        ListRiskWordsResponse result = list();

        thenResponseContainsMappedItemAndMetadata(result);
    }

    // --- arrange ---

    private void givenServiceReturnsEmptyPage() {
        when(riskWordService.list(WORD_FILTER, ACTIVE_FILTER, SEVERITY_FILTER, expectedPageable()))
                .thenReturn(new PageImpl<>(List.of()));
    }

    private void givenServiceReturnsPopulatedPage() {
        Page<RiskWord> page = new PageImpl<>(List.of(riskWord()), expectedPageable(), 1);
        when(riskWordService.list(WORD_FILTER, ACTIVE_FILTER, SEVERITY_FILTER, expectedPageable()))
                .thenReturn(page);
    }

    private ListRiskWordsRequest listRequest() {
        return new ListRiskWordsRequest(WORD_FILTER, ACTIVE_FILTER, SEVERITY_FILTER, PAGE, SIZE);
    }

    private Pageable expectedPageable() {
        return PageRequest.of(PAGE, SIZE);
    }

    private RiskWord riskWord() {
        return RiskWord.builder()
                .id(ITEM_ID)
                .word(ITEM_WORD)
                .description(ITEM_DESCRIPTION)
                .severity(ITEM_SEVERITY)
                .active(true)
                .build();
    }

    // --- act ---

    private ListRiskWordsResponse list() {
        return listRiskWordsUseCase.execute(listRequest());
    }

    // --- assert ---

    private void thenServiceQueriedWithFiltersAndPageable(ListRiskWordsResponse result) {
        assertThat(result).isNotNull();
        verify(riskWordService).list(WORD_FILTER, ACTIVE_FILTER, SEVERITY_FILTER, expectedPageable());
    }

    private void thenResponseContainsMappedItemAndMetadata(ListRiskWordsResponse result) {
        assertThat(result.content()).hasSize(1);
        RiskWordItem item = result.content().get(0);
        assertThat(item.id()).isEqualTo(ITEM_ID);
        assertThat(item.word()).isEqualTo(ITEM_WORD);
        assertThat(item.description()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(item.severity()).isEqualTo(ITEM_SEVERITY);
        assertThat(item.active()).isTrue();
        assertThat(result.pageNumber()).isEqualTo(PAGE);
        assertThat(result.pageSize()).isEqualTo(SIZE);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }
}
