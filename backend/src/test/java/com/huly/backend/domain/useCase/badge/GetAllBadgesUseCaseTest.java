package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.mapper.badge.GetAllBadgesMapper;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllBadgesUseCaseTest {

    @Mock
    private BadgeRepository badgeRepository;
    private GetAllBadgesUseCase getAllBadgesUseCase;

    @BeforeEach
    void setUp() {
        getAllBadgesUseCase = new GetAllBadgesUseCase(badgeRepository, new GetAllBadgesMapper());
    }

    @Test
    void execute_shouldReturnAllBadges() {
        List<Badge> badges = List.of(
                Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build(),
                Badge.builder().id(2L).code("VALENTÍA").name("Valentía").build()
        );
        when(badgeRepository.findAll()).thenReturn(badges);
        GetAllBadgesResponse result = getAllBadgesUseCase.execute();
        assertThat(result.badges()).hasSize(2);
        assertThat(result.badges().get(0).code()).isEqualTo("PRIMER_PASO");
        assertThat(result.badges().get(1).code()).isEqualTo("VALENTÍA");
        verify(badgeRepository).findAll();
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoBadgesExist() {
        when(badgeRepository.findAll()).thenReturn(List.of());
        GetAllBadgesResponse result = getAllBadgesUseCase.execute();
        assertThat(result.badges()).isEmpty();
        verify(badgeRepository).findAll();
    }

}
